package com.ifedorov.neural_network;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DataSet {

    public final List<BigDecimalWrapper> input;
    public final List<BigDecimalWrapper> output;

    public DataSet(List<BigDecimalWrapper> input, List<BigDecimalWrapper> output) {
        this.input = input;
        this.output = output;
    }

}
