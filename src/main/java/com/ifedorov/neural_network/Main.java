package com.ifedorov.neural_network;

import com.google.common.collect.Lists;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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

            @CommandLine.Option(names = {"--trainingAccuracy" }, description = "Path to file with training data set", required = true)
            private Double accuracy;

            @CommandLine.Option(names = {"--trainingEpochs" }, description = "Path to file with training data set", required = true)
            private int epochs;
        }

        static class Predict {
            @CommandLine.Option(names = {"--predictSetFile" }, description = "Path to file with test data set", required = true)
            private File predictSetFile;

            @CommandLine.Option(names = {"--predictOutputFile" }, description = "Path to file with test data set", required = true)
            private File predictOutputFile;
        }

        static class Test {
            @CommandLine.Option(names = {"--testSetFile" }, description = "Path to file with test data set", required = true)
            private File predictSetFile;

            @CommandLine.Option(names = {"--testOutputFile" }, description = "Path to file with test data set", required = true)
            private File predictOutputFile;
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
            TrainingResult result = model
                    .train(
                            normalizedTrainingSet,
                            new BaseStopIndicator(train.epochs, Lists.newArrayList(), new BigDecimalWrapper(train.accuracy))
                    );
            System.out.println("accuracy: " + result.accuracy);
            System.out.println("epochs: " + result.epochs);
            model.printState();
            model.saveTo(train.trainingOutputFile.toPath());
        }
        if(test != null) {
            List<NormalizedTrainingDataSet> normalizedTrainingSet = NormalizedTrainingDataSet.normalize(TrainingDataSet.loadFromTextFile(train.trainingSetFile.toPath()));
            NormalizedTrainingDataSet.saveTo(normalizedTrainingSet, options.normalizedTrainingSetOutputFile);
            model.test(normalizedTrainingSet);
        }
        if(predict != null){
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
