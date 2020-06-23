package com.ifedorov.neural_network.train;

import com.ifedorov.neural_network.train.dataset.*;
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
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static com.ifedorov.neural_network.train.QualityCalculator.*;

public class Main {

    public static class Options {
        @CommandLine.Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
        private boolean helpRequested = false;

        @CommandLine.Option(names = {"--model" }, required = true, description = "Path to file with Neural Network weights/neurons configuration")
        private File modelInputFile;

        @CommandLine.ArgGroup(exclusive = true, validate = true)
        private Mode executionMode;

        static class Mode {
            @CommandLine.ArgGroup(exclusive = false)
            ConvertModel convert;

            @CommandLine.ArgGroup(exclusive = false)
            Training training;

            @CommandLine.ArgGroup(exclusive = false)
            Predict predict;

            @CommandLine.ArgGroup(exclusive = false)
            Test test;
        }

        static class ConvertModel {
            enum Type {
                json, xlsx
            }

            @CommandLine.Option(names = {"--convert-from-type" }, description = "The type of the input model file", required = true)
            private Type fromType;
            @CommandLine.Option(names = {"--convert-to-type" }, description = "The type of the input model file", required = true)
            private Type toType;
            @CommandLine.Option(names = {"--convert-result-file" }, description = "Output model file", required = true)
            private File outputFile;
        }

        static class Training {


            @CommandLine.Option(names = {"--trainSetFile" }, description = "Path to file with training data set", required = true)
            private File trainingSetFile;

            @CommandLine.Option(names = {"--trainingOutputFile" }, description = "Path to file with training data set", required = true)
            private File trainingOutputFile;

//            @CommandLine.Option(names = {"--trainingAccuracy" }, description = "Path to file with training data set", required = false)
//            private Double accuracy;

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

            @CommandLine.Option(names = {"--normalizedTrainSetFile" }, description = "Path where to save normalized training data set", required = true)
            private File normalizedTrainingSetOutputFile;
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

    public static void main(String[] args) throws IOException {
        Options options = new Options();
        CommandLine cmd = new CommandLine(options);
        cmd.setResourceBundle(Utf8ResourceBundle.getBundle("Messages"));
        cmd.parseArgs(args);
        if(cmd.isUsageHelpRequested()) {
            cmd.usage(System.out);
            return;
        }
//        if(options.executionMode == null) {
//            throw new RuntimeException("Please specify one of the execution modes");
//        }
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
        TestResult testResult = model.test(normalizedDataSet);
        testResult.saveTo(test.testOutputFile);
        List<BigDecimalWrapper> expectedOutputs = normalizedDataSet.stream().map(dataSet -> dataSet.getOutputValues().get(0)).collect(Collectors.toList());
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
            List<BigDecimalWrapper> expectedOutputs = testResult.dataSets.stream().map(testSet -> testSet.getOutputValues().get(0)).collect(Collectors.toList());
            List<BigDecimalWrapper> actualOutputs = testResult.dataSets.stream().map(o -> o.getActualOutput().get(0)).collect(Collectors.toList());
            System.out.println(new QualityCalculator().calculateQuality(expectedOutputs, actualOutputs));
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
