package com.ifedorov.neural_network.train;

import com.ifedorov.neural_network.train.dataset.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import picocli.CommandLine;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static com.ifedorov.neural_network.train.QualityCalculator.*;

public class Main {

    public static final ResourceBundle localization = Utf8ResourceBundle.getBundle("Messages");

    public static void main(String[] args) throws IOException {
        Options options = new Options();
        CommandLine cmd = new CommandLine(options);
        cmd.setResourceBundle(localization);
        cmd.parseArgs(args);
        if(cmd.isUsageHelpRequested()) {
            cmd.usage(System.out);
            return;
        }
        Options.Training train = options.executionMode.training;
        Options.Predict predict = options.executionMode.predict;
        Options.Test test = options.executionMode.test;
        if(options.executionMode.convert != null) {
            convert(options.modelInputFile, options.executionMode.convert);
        } else if(train != null) {
            train(train, Model.load(options.modelInputFile.toPath()));
        } else if(test != null) {
            test(test, Model.load(options.modelInputFile.toPath()));
        } else if(predict != null){
            predict(predict, Model.load(options.modelInputFile.toPath()));
        }
    }

    private static void predict(Options.Predict predict, Model model) {
        List<NormalizedPredictionDataSet> predictionDataSets = NormalizedPredictionDataSet.asNormalized(PredictionDataSet.loadFromXLSFile(predict.predictSetFile.toPath(), model.getInputDimension()));
        try (XSSFWorkbook workbook = new XSSFWorkbook();
            OutputStream os = new FileOutputStream(predict.predictOutputFile)) {
            XSSFSheet sheet = workbook.createSheet();
            for (int i = 0; i < predictionDataSets.size(); i++) {
                PredictionDataSet result = model.predict(predictionDataSets.get(i));
                XSSFRow row = sheet.createRow(i);
                List<BigDecimalWrapper> inputs = result.getInputValues();
                for (int j = 0; j < inputs.size(); j++) {
                    row.createCell(j).setCellValue(inputs.get(j).bigDecimal().doubleValue());
                }
                List<BigDecimalWrapper> outputs = result.getOutput();
                int shift = inputs.size();
                for (int j = 0; j < outputs.size(); j++) {
                    row.createCell(shift + j).setCellValue(outputs.get(j).bigDecimal().doubleValue());
                }
            }
            workbook.write(os);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write prediction results", e);
        }
    }

    private static void test(Options.Test test, Model model) {
        List<NormalizedTrainingDataSet> normalizedDataSet = NormalizedTrainingDataSet.asNormalized(
                TrainingDataSet.loadFromXLSFile(test.testSetFile.toPath(), model.getInputDimension(), model.getOutputDimension())
        );
        testModel(model, normalizedDataSet, test.testOutputFile);
    }

    private static void testModel(Model model, List<NormalizedTrainingDataSet> normalizedDataSet, File testOutputFile) {
        TestResult testResult = model.test(normalizedDataSet);
        testResult.saveTo(testOutputFile);
        List<BigDecimalWrapper> expectedOutputs = testResult.dataSets.stream().map(dataSet -> dataSet.getOutputValues().get(0)).collect(Collectors.toList());
        List<BigDecimalWrapper> actualOutputs = testResult.dataSets.stream().map(o -> o.getActualOutput().get(0)).collect(Collectors.toList());
        System.out.println(new QualityCalculator().calculateQuality(expectedOutputs, actualOutputs));
    }

    private static void train(Options.Training train, Model model) {
        List<NormalizedTrainingDataSet> normalizedTrainingSet = NormalizedTrainingDataSet.asNormalized(
                TrainingDataSet.loadFromXLSFile(train.trainingSetFile.toPath(), model.getInputDimension(), model.getOutputDimension())
        );
        NormalizedTrainingDataSet.saveTo(normalizedTrainingSet, train.normalizedTrainingSetOutputFile);
        List<NormalizedTrainingDataSet> normalizedTestSet = NormalizedTrainingDataSet.asNormalized(
                TrainingDataSet.loadFromXLSFile(train.testSetFile.toPath(), model.getInputDimension(), model.getOutputDimension())
        );
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             OutputStream outputStream = new FileOutputStream(train.statisticsFile)){
            XSSFSheet sheet = workbook.createSheet();
            XSSFRow firstRow = sheet.createRow(0);
            firstRow.createCell(0).setCellValue(localization.getString("epoch"));
            firstRow.createCell(1).setCellValue(localization.getString("error"));
            firstRow.createCell(2).setCellValue(localization.getString("accuracy"));
            firstRow.createCell(3).setCellValue(localization.getString("adequacy"));
            firstRow.createCell(4).setCellValue(localization.getString("specificity"));
            firstRow.createCell(5).setCellValue(localization.getString("average"));
           StatisticWriter statisticWriter = new StatisticWriter() {

                @Override
                public void write(long epoch, BigDecimal accuracy, Quality calculatedQuality) {
                    XSSFRow row = sheet.createRow((int) epoch + 1);
                    row.createCell(0).setCellValue(epoch);
                    row.createCell(1).setCellValue(accuracy.doubleValue());
                    if(calculatedQuality != null) {
                        row.createCell(2).setCellValue(calculatedQuality.accuracy.doubleValue());
                        row.createCell(3).setCellValue(calculatedQuality.adequacy.doubleValue());
                        row.createCell(4).setCellValue(calculatedQuality.specificity.doubleValue());
                        row.createCell(5).setCellValue(calculatedQuality.average.doubleValue());
                    }
                }
            };
            BaseStopIndicator stopIndicator = new BaseStopIndicator(train.epochs, train.epochsBetweenTest, new Quality(BigDecimal.valueOf(0.95), BigDecimal.valueOf(0.95), BigDecimal.valueOf(0.95), BigDecimal.valueOf(0.95)));
            TrainingResult result = model
                    .train(
                            normalizedTrainingSet,
                            normalizedTestSet,
                            stopIndicator,
                            statisticWriter
                    );
            System.out.println(localization.getString("accuracy") + ": " + result.accuracy);
            System.out.println(localization.getString("epochs") + ": " + result.epochs);
            System.out.println();
            model.printState();
            testModel(model, normalizedTestSet, train.testOutputFile);
            model.saveTo(train.trainingOutputFile.toPath());
            workbook.write(outputStream);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void convert(File modelInputFile, Options.ConvertModel convert) throws IOException {
            if(convert.fromType == Options.ConvertModel.Type.json) {
                JsonModel.fromJson(modelInputFile).toModel().saveTo(convert.outputFile.toPath());
            } else {
                Files.write(convert.outputFile.toPath(), Model.load(modelInputFile.toPath()).toJsonModel().toJson().getBytes(StandardCharsets.UTF_8));
            }
    }
}
