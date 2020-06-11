package com.ifedorov.neural_network;

import com.google.common.collect.Lists;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import picocli.CommandLine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.ifedorov.neural_network.QualityCalculator.*;

public class Main {

    public static class Options {
        @CommandLine.Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
        private boolean helpRequested = false;

        @CommandLine.Option(names = {"--model" }, required = true, description = "Path to file with Neural Network weights/neurons configuration")
        private File modelInputFile;

        @CommandLine.ArgGroup(exclusive = false, validate = true)
        private Mode executionMode;

        @CommandLine.Option(names = {"--normalizedTrainSetFile" }, description = "Path where to save normalized training data set", required = true)
        private File normalizedTrainingSetOutputFile;

        static class Mode {
            @CommandLine.ArgGroup(exclusive = false)
            Training training;

            @CommandLine.ArgGroup(exclusive = false)
            Predict predict;

            @CommandLine.ArgGroup(exclusive = false)
            Test test;
        }

        static class Training {
            @CommandLine.Option(names = {"--trainSetFile" }, description = "Path to file with training data set", required = true)
            private File trainingSetFile;

            @CommandLine.Option(names = {"--trainingOutputFile" }, description = "Path to file with training data set", required = true)
            private File trainingOutputFile;

            @CommandLine.Option(names = {"--trainingAccuracy" }, description = "Path to file with training data set", required = false)
            private Double accuracy;

            @CommandLine.Option(names = {"--trainingTestSetFile" }, description = "Path to file with test data set", required = true)
            private File testSetFile;

            @CommandLine.Option(names = {"--trainingTestOutputFile" }, description = "Path to file with test data set", required = true)
            private File testOutputFile;

            @CommandLine.Option(names = {"--trainingEpochs" }, description = "Path to file with training data set", required = true)
            private int epochs;

            @CommandLine.Option(names = {"--trainingEpochsBetweenTest" }, description = "Path to file with training data set", required = true)
            private int epochsBetweenTest;
            @CommandLine.Option(names = {"--statisticsFile" }, description = "Path to file with test data set", required = true)
            private File statisticsFile;
        }

        static class Predict {
            @CommandLine.Option(names = {"--predictSetFile" }, description = "Path to file with test data set", required = true)
            private File predictSetFile;

            @CommandLine.Option(names = {"--predictOutputFile" }, description = "Path to file with test data set", required = true)
            private File predictOutputFile;
        }

        static class Test {
            @CommandLine.Option(names = {"--testSetFile" }, description = "Path to file with test data set", required = true)
            private File testSetFile;

            @CommandLine.Option(names = {"--testOutputFile" }, description = "Path to file with test data set", required = true)
            private File testOutputFile;
        }
    }

    public static void main(String[] args) {
        Options options = new Options();
        new CommandLine(options).parseArgs(args);
        if(options.executionMode == null) {
            throw new RuntimeException("Please specify one of the execution modes");
        }
        Options.Training train = options.executionMode.training;
        Options.Predict predict = options.executionMode.predict;
        Options.Test test = options.executionMode.test;
        Model model = Model.load(options.modelInputFile.toPath());
        if(train != null) {
            List<TrainingDataSet> trainingDataSets = TrainingDataSet.loadFromXLSFile(train.trainingSetFile.toPath(), model.getInputDimension(), model.getOutputDimension());
            List<NormalizedTrainingDataSet> normalizedTrainingSet = NormalizedTrainingDataSet.normalize(trainingDataSets);
            NormalizedTrainingDataSet.saveTo(normalizedTrainingSet, options.normalizedTrainingSetOutputFile);
            List<NormalizedTrainingDataSet> normalizedTestSet = NormalizedTrainingDataSet.normalize(TrainingDataSet.loadFromXLSFile(train.testSetFile.toPath(), model.getInputDimension(), model.getOutputDimension()));
            try (XSSFWorkbook workbook = new XSSFWorkbook();
                 OutputStream outputStream = new FileOutputStream(train.statisticsFile)){
                XSSFSheet sheet = workbook.createSheet();
                XSSFRow firstRow = sheet.createRow(0);
                firstRow.createCell(0).setCellValue("epoch");
                firstRow.createCell(1).setCellValue("accuracy");
                firstRow.createCell(2).setCellValue("accuracy");
                firstRow.createCell(3).setCellValue("adequacy");
                firstRow.createCell(4).setCellValue("specificity");
                firstRow.createCell(5).setCellValue("average");
                BaseStopIndicator.Listener listener = new BaseStopIndicator.Listener() {

                    @Override
                    public void statistics(long epoch, BigDecimal accuracy, Quality calculatedQuality) {
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
                TrainingResult result = model
                        .train(
                                normalizedTrainingSet,
                                new BaseStopIndicator(train.epochs, train.epochsBetweenTest, normalizedTestSet,
                                        new Quality(BigDecimal.valueOf(0.95), BigDecimal.valueOf(0.95), BigDecimal.valueOf(0.95), BigDecimal.valueOf(0.95)),
                                        listener)
                        );
                System.out.println("accuracy: " + result.accuracy);
                System.out.println("epochs: " + result.epochs);
                model.printState();
                TestResult testResult = model.test(normalizedTestSet);
                testResult.saveTo(train.testOutputFile);
                List<BigDecimalWrapper> expectedOutputs = testResult.trainingDataSets.stream().map(testSet -> testSet.expectedOutput.get(0)).collect(Collectors.toList());
                List<BigDecimalWrapper> actualOutputs = testResult.trainingDataSets.stream().map(o -> o.getActualOutput().get(0)).collect(Collectors.toList());
                System.out.println(new QualityCalculator().calculateQuality(expectedOutputs, actualOutputs));
                model.saveTo(train.trainingOutputFile.toPath());
                workbook.write(outputStream);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } else if(test != null) {
            List<NormalizedTrainingDataSet> normalizedTrainingSet = NormalizedTrainingDataSet.normalize(TrainingDataSet.loadFromXLSFile(test.testSetFile.toPath(), model.getInputDimension(), model.getOutputDimension()));
            NormalizedTrainingDataSet.saveTo(normalizedTrainingSet, options.normalizedTrainingSetOutputFile);
            TestResult testResult = model.test(normalizedTrainingSet);
            System.out.println(testResult.trainingDataSets.stream().map(o -> o.getActualOutput().get(0)).collect(Collectors.toList()));

            testResult.saveTo(test.testOutputFile);
            List<BigDecimalWrapper> expectedOutputs = normalizedTrainingSet.stream().map(normalizedTrainingDataSet -> normalizedTrainingDataSet.expectedOutput.get(0)).collect(Collectors.toList());
            List<BigDecimalWrapper> actualOutputs = testResult.trainingDataSets.stream().map(o -> o.getActualOutput().get(0)).collect(Collectors.toList());
            System.out.println(new QualityCalculator().calculateQuality(expectedOutputs, actualOutputs));
        } else if(predict != null){
            DecimalFormat formatter = new DecimalFormat("###.###", DecimalFormatSymbols.getInstance(Locale.US));
            List<String> outputLines =
                    readPredictionInput(predict.predictSetFile.toPath())
                    .stream()
                    .map(dataSet -> model
                            .predict(dataSet)
                            .getOutput()
                            .stream()
                            .map(BigDecimalWrapper::bigDecimal)
                            .map(BigDecimal::doubleValue)
                            .map(formatter::format))
                    .map(stringStream -> stringStream.reduce((s, s2) -> s + "," + s2).get())
                    .collect(Collectors.toList());
            try {
                Files.write(predict.predictOutputFile.toPath(), outputLines);
            } catch (IOException e) {
                throw new RuntimeException("Unable to save results");
            }
        }
    }

    private static List<PredictionDataSet> readPredictionInput(Path file) {
        try {
            return Files.readAllLines(file).stream()
                    .map(line -> {
                        return new PredictionDataSet(Arrays.stream(line.split(","))
                                .map(Double::valueOf)
                                .map(BigDecimalWrapper::new)
                                .collect(Collectors.toList()));
                    }).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Unable to read input values from " + file);
        }
    }
}
