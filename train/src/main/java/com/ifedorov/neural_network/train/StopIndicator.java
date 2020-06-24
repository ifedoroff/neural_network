package com.ifedorov.neural_network.train;

public interface StopIndicator {
    boolean shouldStopTraining(QualityCalculator.Quality quality, long epoch);
}
