package com.ifedorov.neural_network;

import java.util.List;

public class BaseStopIndicator implements StopIndicator {

    private final long epochs;
    private final List<NormalizedTrainingDataSet> testDataSet;
    private BigDecimalWrapper requireAccuracy;

    public BaseStopIndicator(long epochs, List<NormalizedTrainingDataSet> testDataSet, BigDecimalWrapper requireAccuracy) {
        this.epochs = epochs;
        this.testDataSet = testDataSet;
        this.requireAccuracy = requireAccuracy;
    }


    @Override
    public boolean shouldStopTraining(Model model, long epoch, BigDecimalWrapper accuracy) {
        return epoch >= epochs || requireAccuracy.compareTo(accuracy) >= 0;
    }

}
