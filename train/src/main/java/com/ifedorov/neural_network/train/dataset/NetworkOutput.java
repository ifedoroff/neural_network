package com.ifedorov.neural_network.train.dataset;

import com.ifedorov.neural_network.train.BigDecimalWrapper;

import java.util.List;

public interface NetworkOutput {
    List<BigDecimalWrapper> getOutputValues();
}
