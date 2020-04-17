package com.ifedorov.neural_network;

import java.util.List;

public class PredictionDataSet {

    public final List<BigDecimalWrapper> input;
    private List<BigDecimalWrapper> output;

    public PredictionDataSet(List<BigDecimalWrapper> input) {
        this.input = input;
    }

    public List<BigDecimalWrapper> getInput() {
        return input;
    }

    public List<BigDecimalWrapper> getOutput() {
        return output;
    }

    public void setOutput(List<BigDecimalWrapper> output) {
        this.output = output;
    }
}
