package com.ifedorov.neural_network.train;

import com.ifedorov.neural_network.train.dataset.NormalizedTrainingDataSet;
import com.ifedorov.neural_network.train.dataset.TrainingDataSet;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class BaseStopIndicator implements StopIndicator {

    private final long epochs;
    private int epochsBetweenCheck;
    private QualityCalculator.Quality threshold;

    public BaseStopIndicator(long epochs, int epochsBetweenCheck, QualityCalculator.Quality threshold) {
        this.epochs = epochs;
        this.epochsBetweenCheck = epochsBetweenCheck;
        this.threshold = threshold;
    }

    @Override
    public boolean shouldStopTraining(QualityCalculator.Quality quality, long epoch) {
        if(epoch >= epochs) {
            return true;
        }
        if(epoch % epochsBetweenCheck == 0) {
            if(threshold.accuracy.compareTo(quality.accuracy) <= 0 &&
                    threshold.adequacy.compareTo(quality.adequacy) <= 0 &&
                    threshold.specificity.compareTo(quality.specificity) <= 0 &&
                    threshold.average.compareTo(quality.average) <= 0) {
                return true;
            }
        }
        return false;
    }
}
