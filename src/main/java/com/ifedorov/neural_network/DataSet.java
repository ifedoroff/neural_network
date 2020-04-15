package com.ifedorov.neural_network;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DataSet {

    public final List<BigDecimalWrapper> input;
    public final List<BigDecimalWrapper> output;

    public DataSet(List<BigDecimalWrapper> input, List<BigDecimalWrapper> output) {
        this.input = input;
        this.output = output;
    }

    public static List<DataSet> loadFromTextFile(Path path) {
        try {
            return Files.lines(path)
                    .map(DataSet::fromCSVLine)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Unable to read data sets from " + path);
        }
    }

    private static DataSet fromCSVLine(String line) {
        return new DataSet(
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
