package com.ifedorov.neural_network;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TrainingDataSet {

    public final List<BigDecimalWrapper> input;
    public final List<BigDecimalWrapper> output;
    private List<BigDecimalWrapper> actualOutput;
    private BigDecimalWrapper accuracy;

    public TrainingDataSet(List<BigDecimalWrapper> input, List<BigDecimalWrapper> output) {
        this.input = input;
        this.output = output;
    }

    public List<BigDecimalWrapper> getActualOutput() {
        return actualOutput;
    }

    public void setActualOutput(List<BigDecimalWrapper> actualOutput) {
        this.actualOutput = actualOutput;
    }

    public BigDecimalWrapper getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(BigDecimalWrapper accuracy) {
        this.accuracy = accuracy;
    }

    public static List<TrainingDataSet> loadFromTextFile(Path path) {
        try {
            return Files.lines(path)
                    .map(TrainingDataSet::fromCSVLine)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Unable to read data sets from " + path);
        }
    }

    private static TrainingDataSet fromCSVLine(String line) {
        return new TrainingDataSet(
                Arrays.stream(line.split("\\|")[0].split(","))
                        .map(Double::valueOf)
                        .map(BigDecimalWrapper::new)
                        .collect(Collectors.toList()),
                Arrays.stream(line.split("\\|")[1].split(","))
                        .map(Double::valueOf)
                        .map(BigDecimalWrapper::new)
                        .collect(Collectors.toList())
        );
    }

}
