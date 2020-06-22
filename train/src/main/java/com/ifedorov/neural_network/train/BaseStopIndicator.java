package com.ifedorov.neural_network.train;

import com.ifedorov.neural_network.train.dataset.NormalizedTrainingDataSet;
import com.ifedorov.neural_network.train.dataset.TrainingDataSet;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class BaseStopIndicator implements StopIndicator {

    private final long epochs;
    private int epochsBetweenCheck;
    private final List<NormalizedTrainingDataSet> testDataSet;
    private QualityCalculator.Quality threshold;
    private Listener listener;

    public BaseStopIndicator(long epochs, int epochsBetweenCheck, List<NormalizedTrainingDataSet> testDataSet, QualityCalculator.Quality threshold, Listener listener) {
        this.epochs = epochs;
        this.epochsBetweenCheck = epochsBetweenCheck;
        this.testDataSet = testDataSet;
        this.threshold = threshold;
        this.listener = listener;
    }

    @Override
    public boolean shouldStopTraining(Model model, long epoch, BigDecimalWrapper accuracy) {
        if(epoch >= epochs) {
            listener.statistics(epoch, accuracy.bigDecimal(), calculateQuality(model));
            return true;
        }
        if(epoch % epochsBetweenCheck == 0) {
            QualityCalculator.Quality quality = calculateQuality(model);
            listener.statistics(epoch, accuracy.bigDecimal(), quality);
            if(threshold.accuracy.compareTo(quality.accuracy) <= 0 &&
                    threshold.adequacy.compareTo(quality.adequacy) <= 0 &&
                    threshold.specificity.compareTo(quality.specificity) <= 0 &&
                    threshold.average.compareTo(quality.average) <= 0) {
                return true;
            }
        } else {
            listener.statistics(epoch, accuracy.bigDecimal(), null);
        }
        return false;
    }

    private QualityCalculator.Quality calculateQuality(Model model) {
        List<? extends TrainingDataSet> testResult = model.test(testDataSet).dataSets;
        List<BigDecimalWrapper> actualOutput = testResult.stream().map(TrainingDataSet::getActualOutput).map(list -> list.get(0)).collect(Collectors.toList());
        List<BigDecimalWrapper> expectedOutput = testResult.stream().map(o -> o.getOutputValues()).map(list -> list.get(0)).collect(Collectors.toList());
        return new QualityCalculator().calculateQuality(expectedOutput, actualOutput);
    }

    public interface Listener {
        void statistics(long epoch, BigDecimal accuracy, QualityCalculator.Quality calculatedQuality);
    }

}
