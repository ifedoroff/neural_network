package com.ifedorov.neural_network;

import java.math.BigDecimal;
import java.util.List;

public class TrainingDataSet {

    public final List<BigDecimal> input;
    public final List<BigDecimal> output;

    public TrainingDataSet(List<BigDecimal> input, List<BigDecimal> output) {
        this.input = input;
        this.output = output;
    }

}
