package com.ifedorov.neural_network.train;

import picocli.CommandLine;

import java.io.File;

class Options {
    @CommandLine.Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
    boolean helpRequested = false;

    @CommandLine.Option(names = {"--model" }, required = true, description = "Path to file with Neural Network weights/neurons configuration")
    File modelInputFile;

    @CommandLine.ArgGroup(exclusive = true, validate = true)
    Mode executionMode;

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
        ConvertModel.Type fromType;
        @CommandLine.Option(names = {"--convert-to-type" }, description = "The type of the input model file", required = true)
        ConvertModel.Type toType;
        @CommandLine.Option(names = {"--convert-result-file" }, description = "Output model file", required = true)
        File outputFile;
    }

    static class Training {


        @CommandLine.Option(names = {"--trainSetFile" }, description = "Path to file with training data set", required = true)
        File trainingSetFile;

        @CommandLine.Option(names = {"--trainingOutputFile" }, description = "Path to file with training data set", required = true)
        File trainingOutputFile;

//            @CommandLine.Option(names = {"--trainingAccuracy" }, description = "Path to file with training data set", required = false)
//            Double accuracy;

        @CommandLine.Option(names = {"--trainingTestSetFile" }, description = "Path to file with test data set", required = true)
        File testSetFile;

        @CommandLine.Option(names = {"--trainingTestOutputFile" }, description = "Path to file with test data set", required = true)
        File testOutputFile;

        @CommandLine.Option(names = {"--trainingEpochs" }, description = "Path to file with training data set", required = true)
        int epochs;

        @CommandLine.Option(names = {"--trainingEpochsBetweenTest" }, description = "Path to file with training data set", required = true)
        int epochsBetweenTest;

        @CommandLine.Option(names = {"--statisticsFile" }, description = "Path to file with test data set", required = true)
        File statisticsFile;

        @CommandLine.Option(names = {"--normalizedTrainSetFile" }, description = "Path where to save normalized training data set", required = true)
        File normalizedTrainingSetOutputFile;
    }

    static class Predict {
        @CommandLine.Option(names = {"--predictSetFile" }, description = "Path to file with test data set", required = true)
        File predictSetFile;

        @CommandLine.Option(names = {"--predictOutputFile" }, description = "Path to file with test data set", required = true)
        File predictOutputFile;
    }

    static class Test {
        @CommandLine.Option(names = {"--testSetFile" }, description = "Path to file with test data set", required = true)
        File testSetFile;

        @CommandLine.Option(names = {"--testOutputFile" }, description = "Path to file with test data set", required = true)
        File testOutputFile;
    }
}
