package com.ifedorov.neural_network;

public interface StopIndicator {
    boolean shouldStopTraining(Model model, long epoch, BigDecimalWrapper accuracy);
}
