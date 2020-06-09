package com.ifedorov.neural_network;

import java.util.List;

public class TrainingResult {
    public final long epochs;
    public final BigDecimalWrapper accuracy;
    public List<? extends TrainingDataSet> trainingDataSets;

    public TrainingResult(long epochs, BigDecimalWrapper accuracy, List<? extends TrainingDataSet> trainingDataSets) {
        this.epochs = epochs;
        this.accuracy = accuracy;
        this.trainingDataSets = trainingDataSets;
    }
}
