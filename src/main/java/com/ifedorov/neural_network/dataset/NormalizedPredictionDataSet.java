package com.ifedorov.neural_network.dataset;

import com.ifedorov.neural_network.BigDecimalWrapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NormalizedPredictionDataSet extends PredictionDataSet {
    public NormalizedPredictionDataSet(PredictionDataSet input) {
        super(input.getInputValues());
        ensureNormalized(input.getInputValues());
    }

    private void ensureNormalized(List<BigDecimalWrapper> values) {
        Optional<BigDecimalWrapper> denormalized = values.stream().filter(value -> value.compareTo(BigDecimalWrapper.ZERO) < 0 || value.compareTo(BigDecimalWrapper.ONE) > 0)
                .findFirst();
        if(denormalized.isPresent()) {
            throw new IllegalArgumentException("One of the values is not normalized: " + values);
        }
    }

    public static List<NormalizedPredictionDataSet> asNormalized(List<PredictionDataSet> predictionDataSets){
        NormalizedTrainingDataSet.normalize(predictionDataSets);
        return predictionDataSets.stream().map(NormalizedPredictionDataSet::new).collect(Collectors.toList());
    }
}
