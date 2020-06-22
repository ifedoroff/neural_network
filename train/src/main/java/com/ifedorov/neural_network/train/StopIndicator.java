package com.ifedorov.neural_network.train;

public interface StopIndicator {
    boolean shouldStopTraining(Model model, long epoch, BigDecimalWrapper accuracy);
}
