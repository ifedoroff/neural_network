package com.ifedorov.neural_network;

import picocli.CommandLine;

import java.io.File;

public class Main {

    public static class Options {
        @CommandLine.Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
        private boolean helpRequested = false;

        @CommandLine.Option(names = {"--trainSetFile" }, description = "Path to file with training data set")
        private File trainingSetFile;

        @CommandLine.Option(names = {"--trainOutputFile" }, description = "Path to file with training data set")
        private File trainingOutputFile;

        @CommandLine.Option(names = {"--testSetFile" }, description = "Path to file with test data set")
        private File testSetFile;

        @CommandLine.Option(names = {"--testOutputFile" }, description = "Path to file with test data set")
        private File testOutputFile;

        @CommandLine.Option(names = {"--neuralNetwork" }, description = "Path to file with Neural Network weights/neurons configuration")
        private File modelInputFile;
    }

    public static void main(String[] args) {
        Options options = new Options();
        new CommandLine(options).parseArgs(args);
    }
}
