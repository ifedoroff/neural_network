package com.ifedorov.neural_network;

import java.util.List;

public class TestResult {
    public final BigDecimalWrapper accuracy;
    public List<? extends TrainingDataSet> trainingTrainingDataSets;

    public TestResult(BigDecimalWrapper accuracy, List<? extends TrainingDataSet> trainingTrainingDataSets) {
        this.accuracy = accuracy;
        this.trainingTrainingDataSets = trainingTrainingDataSets;
    }
}
