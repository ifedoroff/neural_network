package com.ifedorov.neural_network;

import java.util.List;

public class TrainingResult {
    public final long epochs;
    public final BigDecimalWrapper accuracy;
    public List<TrainingDataSet> trainingTrainingDataSets;

    public TrainingResult(long epochs, BigDecimalWrapper accuracy, List<TrainingDataSet> trainingTrainingDataSets) {
        this.epochs = epochs;
        this.accuracy = accuracy;
        this.trainingTrainingDataSets = trainingTrainingDataSets;
    }
}
