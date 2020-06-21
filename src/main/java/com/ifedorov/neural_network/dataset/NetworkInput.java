package com.ifedorov.neural_network.dataset;

import com.ifedorov.neural_network.BigDecimalWrapper;

import java.util.List;

public interface NetworkInput {

    List<BigDecimalWrapper> getInputValues();
}
