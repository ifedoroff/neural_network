package com.ifedorov.neural_network.train;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.ifedorov.neural_network.train.dataset.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class Model {

    private BigDecimalWrapper learningFactor;
    private LinkedList<List<Neuron>> tiers = new LinkedList<>();
    private List<WeightMatrix> weightMatrices;

    public static final ResourceBundle localization = Utf8ResourceBundle.getBundle("Messages");

    public Model(List<List<Neuron>> tiers, List<WeightMatrix> weightMatrices, BigDecimalWrapper learningFactor) {
        this.learningFactor = learningFactor;
        if(tiers.size() != weightMatrices.size())
            throw new IllegalArgumentException("Number of Tiers should be equals to number of Weight matrices");
        this.tiers = new LinkedList<>(tiers);
        this.weightMatrices = weightMatrices;
    }

    public int getInputDimension() {
        return weightMatrices.get(0).rowDimension();
    }

    public int getOutputDimension() {
        return tiers.getLast().size();
    }

    public BigDecimalWrapper getLearningFactor() {
        return learningFactor;
    }

    public List<WeightMatrix> weights() {
        return weightMatrices;
    }

    public WeightMatrix weightsForLevel(int level) {
        return weightMatrices.get(level);
    }

    public TrainingResult train(List<NormalizedTrainingDataSet> trainingDataSets, List<NormalizedTrainingDataSet> testDataSet, StopIndicator stopIndicator, StatisticWriter statisticWriter) {
        int epoch = 0;
        BigDecimalWrapper accuracy = BigDecimalWrapper.ONE.add(BigDecimalWrapper.ONE);
        QualityCalculator.Quality quality = QualityCalculator.Quality.ZERO;
        while(!stopIndicator.shouldStopTraining(quality, epoch)) {
            accuracy = BigDecimalWrapper.ZERO;

            for (NormalizedTrainingDataSet trainingDataSet : trainingDataSets) {
                BigDecimalWrapper currentError = this.train(trainingDataSet);
                trainingDataSet.setAccuracy(currentError);
                trainingDataSet.setActualOutput(currentOutputValues());
                accuracy = accuracy.add(currentError);
            }
            accuracy = accuracy.divide(new BigDecimalWrapper(trainingDataSets.size()));
            quality = calculateQuality(testDataSet);
            statisticWriter.write(epoch, accuracy.bigDecimal(), quality);
            epoch++;
        }
        return new TrainingResult(epoch, accuracy, trainingDataSets);
    }

    private QualityCalculator.Quality calculateQuality(List<NormalizedTrainingDataSet> testDataSet) {
        List<? extends TrainingDataSet> testResult = test(testDataSet).dataSets;
        List<BigDecimalWrapper> actualOutput = testResult.stream().map(TrainingDataSet::getActualOutput).map(list -> list.get(0)).collect(Collectors.toList());
        List<BigDecimalWrapper> expectedOutput = testResult.stream().map(o -> o.getOutputValues()).map(list -> list.get(0)).collect(Collectors.toList());
        return new QualityCalculator().calculateQuality(expectedOutput, actualOutput);
    }


    public BigDecimalWrapper train(NormalizedTrainingDataSet trainingDataSet) {
        if(trainingDataSet.getOutputValues().size() != tiers.getLast().size())
            throw new IllegalArgumentException("Number of input values should be equals to the number of Neurons of the first level");
        forwardPass(trainingDataSet.getInputValues());
        backwardPass(trainingDataSet.getOutputValues(), trainingDataSet.getInputValues());
        return calculateAccuracy(trainingDataSet.getOutputValues());
    }

    public PredictionDataSet predict(NormalizedPredictionDataSet dataSet) {
        forwardPass(dataSet.getInputValues());
        dataSet.setOutput(tiers.getLast().stream().map(Neuron::currentValue).collect(Collectors.toList()));
        return dataSet;
    }

    public TestResult test(List<NormalizedTrainingDataSet> trainingDataSets) {
        BigDecimalWrapper accuracy = BigDecimalWrapper.ZERO;
        for (TrainingDataSet trainingDataSet : trainingDataSets) {
            this.forwardPass(trainingDataSet.getInputValues());
            BigDecimalWrapper currentError = calculateAccuracy(trainingDataSet.getOutputValues());
            trainingDataSet.setAccuracy(currentError);
            trainingDataSet.setActualOutput(currentOutputValues());
            accuracy = accuracy.add(currentError);
        }
        accuracy = accuracy.divide(new BigDecimalWrapper(trainingDataSets.size()));
        return new TestResult(accuracy, trainingDataSets);
    }

    private BigDecimalWrapper calculateAccuracy(List<BigDecimalWrapper> expectedOutputs) {
        List<Neuron> outputTier = tiers.getLast();
        return IntStream.range(0, outputTier.size())
                .mapToObj(index -> outputTier.get(index).currentValue().subtract(expectedOutputs.get(index)).pow(2))
                .reduce(BigDecimalWrapper::add).get();
    }

    private void backwardPass(List<BigDecimalWrapper> expectedOutputs, List<BigDecimalWrapper> inputs) {
        adjustWeights(calculateErrors(expectedOutputs), inputs);
    }

    private void adjustWeights(List<List<BigDecimalWrapper>> errors, List<BigDecimalWrapper> inputs) {
        for (int i = weightMatrices.size() - 1; i > 0; i--) {
            WeightMatrix weightMatrix = weightMatrices.get(i);
            List<Neuron> lowerTier = tiers.get(i - 1);
            List<BigDecimalWrapper> levelErrors = errors.get(i);
            weightMatrix.walk(new WeightMatrix.Visitor() {
                @Override
                public BigDecimalWrapper visit(int row, int column, BigDecimalWrapper value) {
                    if(value == null) {
                        return null;
                    } else {
                        BigDecimalWrapper delta = lowerTier.get(row).currentValue().multiply(learningFactor).multiply(levelErrors.get(column));
                        return value.add(delta);
                    }
                }
            });
        }
        WeightMatrix weightMatrix = weightMatrices.get(0);
        List<BigDecimalWrapper> levelErrors = errors.get(0);
        weightMatrix.walk(new WeightMatrix.Visitor() {
            @Override
            public BigDecimalWrapper visit(int row, int column, BigDecimalWrapper value) {
                BigDecimalWrapper delta = inputs.get(row).multiply(learningFactor).multiply(levelErrors.get(column));
                return value.add(delta);
            }
        });
    }

    private List<List<BigDecimalWrapper>> calculateErrors(List<BigDecimalWrapper> expectedOutputs) {
        int tierNumber = tiers.size();
        LinkedList<List<BigDecimalWrapper>> errors = new LinkedList<>();
        List<BigDecimalWrapper> upperLevelError = calculateErrorForOutputTier(expectedOutputs);
        errors.push(upperLevelError);
        for (int i = tierNumber - 2; i >= 0; i--) {
            List<Neuron> currentTier = tiers.get(i);
            WeightMatrix currentMatrix = weightMatrices.get(i + 1);
            WeightMatrix currentToHigherTierWeightMatrix = currentMatrix;
            List<BigDecimalWrapper> finalHigherLevelError = upperLevelError;
            List<BigDecimalWrapper> levelError = IntStream.range(0, currentTier.size())
                    .mapToObj(neuronIndex -> {
                        Neuron neuron = currentTier.get(neuronIndex);
                        List<BigDecimalWrapper> weights = currentToHigherTierWeightMatrix.getWeightForNeuron(neuronIndex);
                        return IntStream.range(0, weights.size())
                                .mapToObj(weightIndex -> {
                                    BigDecimalWrapper weight = weights.get(weightIndex);
                                    if(weight == null) {
                                        return BigDecimalWrapper.ZERO;
                                    } else {
                                        return weight.multiply(finalHigherLevelError.get(weightIndex));
                                    }
                                })
                                .reduce(BigDecimalWrapper::add)
                                .get().multiply(neuron.derivative());
                    }).collect(Collectors.toList());
            errors.push(levelError);
            upperLevelError = levelError;
        }
        return errors;
    }

    private List<BigDecimalWrapper> calculateErrorForOutputTier(List<BigDecimalWrapper> expectedOutputs) {
        List<Neuron> outputTier = tiers.getLast();
        return IntStream.range(0, expectedOutputs.size())
                .mapToObj(index -> {
                    Neuron neuron = outputTier.get(index);
                    BigDecimalWrapper actualValue = neuron.currentValue();
                    BigDecimalWrapper expectedValue = expectedOutputs.get(index);
                    return expectedValue.subtract(actualValue).multiply(neuron.derivative());
                }).collect(Collectors.toList());
    }

    private void forwardPass(List<BigDecimalWrapper> inputs) {
        for (int i = 0; i < tiers.size(); i++) {
            final int tierLevel = i;
            List<Neuron> tierNeurons = tiers.get(tierLevel);
            List<BigDecimalWrapper> finalInputs = inputs;
            inputs = IntStream.range(0, tierNeurons.size())
                    .mapToObj(neuronPosition -> {
                        WeightMatrix weightMatrix = weightMatrices.get(tierLevel);
                        return tierNeurons.get(neuronPosition).calculate(finalInputs, weightMatrix.transposed().getWeightForNeuron(neuronPosition));
                    }).collect(Collectors.toList());
        }
    }

    public void printState() {
        IntStream.range(0, tiers.size())
                .forEach(index -> {
                    System.out.printf("========================%s==========================\n", localization.getString("level") + ": " + (index + 1));
                    System.out.println();
                    System.out.println(localization.getString("weights") + ":");
                    weightMatrices.get(index).printState();
                    System.out.println(localization.getString("neurons") + ":");
                    tiers.get(index).forEach(neuron -> System.out.printf("%4.2f | ", neuron.currentValue().bigDecimal()));
                    System.out.println();
                    System.out.println();
                });
    }

    public static Model load(Path path) {
        try(XSSFWorkbook workbook = new XSSFWorkbook(path.toFile())) {
            return load(workbook);
        } catch (InvalidFormatException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Model load(InputStream is) {
        try(XSSFWorkbook workbook = new XSSFWorkbook(is)) {
            return load(workbook);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Model load(XSSFWorkbook workbook) {
        ModelBuilder builder = new ModelBuilder();

        Iterator<Sheet> sheets = workbook.sheetIterator();
        Sheet firstSheet = sheets.next();
        builder.learningFactor(BigDecimal.valueOf(firstSheet.getRow(0).getCell(1).getNumericCellValue()));
        int inputTierSize = BigDecimal.valueOf(firstSheet.getRow(1).getCell(1).getNumericCellValue()).intValue();
        int outputTierSize = BigDecimal.valueOf(firstSheet.getRow(2).getCell(1).getNumericCellValue()).intValue();
        builder.expectedInputSize(inputTierSize).expectedOutputSize(outputTierSize);
        int previousTierSize = inputTierSize;
        while (sheets.hasNext()) {
            Sheet sheet = sheets.next();
            previousTierSize = loadTier(sheet, builder, previousTierSize);
        }
        return builder.build();

    }


    private static int loadTier(Sheet sheet, ModelBuilder builder, int previousTierSize) {
        Iterator<Row> rows = sheet.rowIterator();
        ModelBuilder.Tier tier = builder.tier();
        Row neuronRow = rows.next();
        StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(neuronRow.cellIterator(), Spliterator.ORDERED),
                false)
                .forEach(cell -> tier.neuron(new Neuron(ActivationFn.fromString(cell.getStringCellValue()))));
        int currentTierSize = tier.neuronCount();
        WeightMatrix.Builder weightBuilder = new WeightMatrix.Builder(previousTierSize, currentTierSize);
        while(rows.hasNext()) {
            Row current = rows.next();
            List<BigDecimal> weights = Lists.newArrayList();
            for (int i = 0; i < currentTierSize; i++) {
                BigDecimal cellValue;
                Cell cell = current.getCell(i);
                if(cell == null) {
                    cellValue = null;
                } else {
                    if(CellType.NUMERIC.getCode() == cell.getCellType()) {
                        cellValue = BigDecimal.valueOf(Double.valueOf(cell.getNumericCellValue()));
                    } else {
                        String value = cell.getStringCellValue();
                        if(Strings.isNullOrEmpty(value)) {
                            cellValue = null;
                        } else {
                            cellValue = BigDecimal.valueOf(Double.valueOf(value));
                        }
                    }
                }
                weights.add(cellValue);
            }
            weightBuilder.row(weights.toArray(new BigDecimal[0]));
        }
        tier.weights(weightBuilder.build()).build();
        return currentTierSize;
    }

    public void saveTo(Path path) {
        try (OutputStream os = new FileOutputStream(path.toFile())){
            saveTo(os);
        } catch (IOException e) {
            throw new RuntimeException("Unable to save model to file", e);
        }
    }

    public void saveTo(OutputStream outputStream) {
        try(XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet firstSheet = workbook.createSheet(localization.getString("learningFactor"));
            XSSFRow firstRow = firstSheet
                    .createRow(0);
            firstRow
                    .createCell(0)
                    .setCellValue(localization.getString("learningFactor"));
            firstRow
                    .createCell(1)
                    .setCellValue(learningFactor.bigDecimal().doubleValue());
            XSSFRow secondRow = firstSheet.createRow(1);
            secondRow.createCell(0).setCellValue(localization.getString("inputDimension"));
            secondRow.createCell(1).setCellValue(getInputDimension());
            XSSFRow thirdRow = firstSheet.createRow(2);
            thirdRow.createCell(0).setCellValue(localization.getString("outputDimension"));
            thirdRow.createCell(1).setCellValue(getOutputDimension());
            for (int i = 0; i < tiers.size(); i++) {
                XSSFSheet sheet = workbook.createSheet(localization.getString("Tier") + " " + (i + 1));
                WeightMatrix weights = weightMatrices.get(i);
                List<Neuron> tier = tiers.get(i);
                XSSFRow neuronRow = sheet.createRow(0);
                IntStream.range(0, tier.size())
                        .forEach(position -> neuronRow.createCell(position).setCellValue(tier.get(position).toString()));
                AtomicInteger rowPosition = new AtomicInteger(0);
                weights.rows()
                        .forEach(weightRow -> {
                            XSSFRow sheetRow = sheet.createRow(rowPosition.incrementAndGet());
                            AtomicInteger cellPosition = new AtomicInteger();
                            weightRow.forEach(weight -> {
                                XSSFCell cell = sheetRow.createCell(cellPosition.getAndIncrement());
                                if(weight == null) {
                                    cell.setCellValue("");
                                } else {
                                    cell.setCellValue(weight.bigDecimal().doubleValue());
                                }
                            });
                        });
            }
            workbook.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public List<BigDecimalWrapper> currentOutputValues() {
        List<Neuron> outputTier = tiers.getLast();
        return IntStream.range(0, outputTier.size())
                .mapToObj(index -> outputTier.get(index).currentValue())
                .collect(Collectors.toList());
    }

    public List<Neuron> tier(int index) {
        return tiers.get(index);
    }

    public JsonModel toJsonModel() {
        JsonModel jsonModel = new JsonModel();
        jsonModel.setLearningFactor(learningFactor.bigDecimal().doubleValue());
        jsonModel.setInputTierSize(getInputDimension());
        List<JsonModel.Tier> jsonTiers = Lists.newArrayList();
        for (int i = 0; i < tiers.size(); i++) {
            List<Neuron> tier = tiers.get(i);
            JsonModel.Tier jsonTier = new JsonModel.Tier();
            jsonTier.setNeurons(tier.stream().map((neuron -> {
                JsonModel.Neuron jsonNeuron = new JsonModel.Neuron();
                if(neuron.getActivationFn().getClass() == ActivationFn.Pseudo.class) {
                    jsonNeuron.setDummy(true);
                } else {
                    jsonNeuron.setActivationFn(neuron.getActivationFn().toString());
                }
                return jsonNeuron;
            })).collect(Collectors.toList()));
            WeightMatrix weightMatrix = weightsForLevel(i);
            final List<JsonModel.Connection> connections = Lists.newArrayList();
            weightMatrix.walk(new WeightMatrix.Visitor() {
                @Override
                public BigDecimalWrapper visit(int row, int column, BigDecimalWrapper value) {
                    if(value != null) {
                        JsonModel.Connection connection = new JsonModel.Connection();
                        connection.setSource(row);
                        connection.setTarget(column);
                        connection.setWeight(value.bigDecimal().doubleValue());
                        connections.add(connection);
                    }
                    return value;
                }
            });
            jsonTier.setConnections(connections);
            jsonTiers.add(jsonTier);
        }
        jsonModel.setTiers(jsonTiers);
        return jsonModel;
    }
}
