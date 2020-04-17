package com.ifedorov.neural_network;

import com.google.common.collect.Lists;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

    public Model(List<List<Neuron>> tiers, List<WeightMatrix> weightMatrices, BigDecimalWrapper learningFactor) {
        this.learningFactor = learningFactor;
        if(tiers.size() != weightMatrices.size())
            throw new IllegalArgumentException("Number of Tiers should be equals to number of Weight matrices");
        this.tiers = new LinkedList<>(tiers);
        this.weightMatrices = weightMatrices;
    }

    public TrainingResult train(List<TrainingDataSet> trainingDataSets, int maxEpochs, BigDecimalWrapper requiredAccuracy) {
        int epoch = 0;
        BigDecimalWrapper accuracy = BigDecimalWrapper.ONE.add(requiredAccuracy);
        while(epoch < maxEpochs && accuracy.compareTo(requiredAccuracy) > 0) {
            accuracy = BigDecimalWrapper.ZERO;
            for (TrainingDataSet trainingDataSet : trainingDataSets) {
                BigDecimalWrapper currentError = this.train(trainingDataSet);
                trainingDataSet.setAccuracy(currentError);
                trainingDataSet.setActualOutput(currentOutputValues());
                accuracy = accuracy.add(currentError);
            }
            accuracy = accuracy.divide(new BigDecimalWrapper(trainingDataSets.size()));
            epoch++;
        }
        return new TrainingResult(epoch, accuracy, trainingDataSets);
    }

    public BigDecimalWrapper train(TrainingDataSet trainingDataSet) {
        if(trainingDataSet.input.size() != tiers.getFirst().size())
            throw new IllegalArgumentException("Number of input values should be equals to the number of Neurons of the first level");
        if(trainingDataSet.output.size() != tiers.getLast().size())
            throw new IllegalArgumentException("Number of input values should be equals to the number of Neurons of the first level");
        forwardPass(trainingDataSet.input);
        backwardPass(trainingDataSet.output, trainingDataSet.input);
        return calculateAccuracy(trainingDataSet.output);
    }

    public PredictionDataSet predict(PredictionDataSet dataSet) {
        forwardPass(dataSet.input);
        dataSet.setOutput(tiers.getLast().stream().map(Neuron::currentValue).collect(Collectors.toList()));
        return dataSet;
    }

    public TestResult test(List<TrainingDataSet> trainingDataSets) {
        BigDecimalWrapper accuracy = BigDecimalWrapper.ZERO;
        for (TrainingDataSet trainingDataSet : trainingDataSets) {
            this.forwardPass(trainingDataSet.input);
            BigDecimalWrapper currentError = calculateAccuracy(trainingDataSet.output);
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

    private void backwardPass(List<BigDecimalWrapper> expectedOutput, List<BigDecimalWrapper> inputs) {
        adjustWeights(calculateErrors(expectedOutput), inputs);
    }

    private void adjustWeights(List<List<BigDecimalWrapper>> errors, List<BigDecimalWrapper> inputs) {
        for (int i = weightMatrices.size() - 1; i > 0; i--) {
            WeightMatrix weightMatrix = weightMatrices.get(i);
            List<Neuron> lowerTier = tiers.get(i - 1);
            List<BigDecimalWrapper> levelErrors = errors.get(i);
            weightMatrix.walk(new WeightMatrix.Visitor() {
                @Override
                public BigDecimalWrapper visit(int row, int column, BigDecimalWrapper value) {
                    BigDecimalWrapper delta = lowerTier.get(row).currentValue().multiply(learningFactor).multiply(levelErrors.get(column));
                    return value.add(delta);
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

    private List<List<BigDecimalWrapper>> calculateErrors(List<BigDecimalWrapper> expectedOutput) {
        int tierNumber = tiers.size();
        LinkedList<List<BigDecimalWrapper>> errors = new LinkedList<>();
        List<BigDecimalWrapper> upperLevelError = calculateErrorForOutputTier(expectedOutput);
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
                                .mapToObj(weightIndex -> weights.get(weightIndex).multiply(finalHigherLevelError.get(weightIndex)))
                                .reduce(BigDecimalWrapper::add)
                                .get().multiply(neuron.derivative());
                    }).collect(Collectors.toList());
            errors.push(levelError);
            upperLevelError = levelError;
        }
        return errors;
    }

    private List<BigDecimalWrapper> calculateErrorForOutputTier(List<BigDecimalWrapper> expectedOutput) {
        List<Neuron> outputTier = tiers.getLast();
        return IntStream.range(0, expectedOutput.size())
                .mapToObj(index -> {
                    Neuron neuron = outputTier.get(index);
                    BigDecimalWrapper actualValue = neuron.currentValue();
                    BigDecimalWrapper expectedValue = expectedOutput.get(index);
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
                    System.out.println("Level: " + index);
                    System.out.println();
                    System.out.println("Weights:");
                    weightMatrices.get(index).printState();
                    System.out.println("Neurons:");
                    tiers.get(index).forEach(neuron -> System.out.printf("%4.2f | ", neuron.currentValue().bigDecimal()));
                    System.out.println();
                    System.out.println();
                });
    }

    public static Model load(Path path) {
        ModelBuilder builder = new ModelBuilder();
        try(XSSFWorkbook workbook = new XSSFWorkbook(path.toFile())) {
            Iterator<Sheet> sheets = workbook.sheetIterator();
            builder.learningFactor(BigDecimal.valueOf(sheets.next().getRow(0).getCell(0).getNumericCellValue()));
            while (sheets.hasNext()) {
                Sheet sheet = sheets.next();
                loadTier(sheet, builder);
            }
            return builder.build();
        } catch (InvalidFormatException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static void loadTier(Sheet sheet, ModelBuilder builder) {
        Iterator<Row> rows = sheet.rowIterator();
        ModelBuilder.Tier tier = builder.tier();
        WeightMatrix.Builder weightBuilder = new WeightMatrix.Builder();
        Row neuronRow = rows.next();
        StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(neuronRow.cellIterator(), Spliterator.ORDERED),
                false)
                .forEach(cell -> tier.neuron(new Neuron(ActivationFn.fromString(cell.getStringCellValue()))));
        while(rows.hasNext()) {
            Row current = rows.next();
            Iterator<Cell> cells = current.cellIterator();
            List<BigDecimal> weights = Lists.newArrayList();
            while(cells.hasNext()) {
                weights.add(BigDecimal.valueOf(Double.valueOf(cells.next().getNumericCellValue())));
            }
            weightBuilder.row(weights.toArray(new BigDecimal[0]));
        }
        tier.weights(weightBuilder.build()).build();
    }

    public void saveTo(Path path) {
        try(XSSFWorkbook workbook = new XSSFWorkbook()) {
            workbook.createSheet("Learning Factor")
                    .createRow(0)
                    .createCell(0)
                    .setCellValue(learningFactor.bigDecimal().doubleValue());
            for (int i = 0; i < tiers.size(); i++) {
                XSSFSheet sheet = workbook.createSheet("Tier " + (i + 1));
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
                            weightRow.forEach(weight -> sheetRow.createCell(cellPosition.getAndIncrement()).setCellValue(weight.bigDecimal().doubleValue()));
                        });
            }
            try (OutputStream os = new FileOutputStream(path.toFile())){
                workbook.write(os);
            }
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
}
