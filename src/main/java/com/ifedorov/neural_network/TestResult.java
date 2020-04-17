package com.ifedorov.neural_network;

import java.util.List;

public class TestResult {
    public final BigDecimalWrapper accuracy;
    public List<TrainingDataSet> trainingTrainingDataSets;

    public TestResult(BigDecimalWrapper accuracy, List<TrainingDataSet> trainingTrainingDataSets) {
        this.accuracy = accuracy;
        this.trainingTrainingDataSets = trainingTrainingDataSets;
    }
}
