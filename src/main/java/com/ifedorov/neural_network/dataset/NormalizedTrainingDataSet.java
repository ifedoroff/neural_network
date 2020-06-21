package com.ifedorov.neural_network.dataset;

import com.google.common.collect.Lists;
import com.ifedorov.neural_network.BigDecimalWrapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NormalizedTrainingDataSet extends TrainingDataSet {

    public NormalizedTrainingDataSet(TrainingDataSet trainingDataSet) {
        this(trainingDataSet.getInputValues(), trainingDataSet.getOutputValues());
    }
    public NormalizedTrainingDataSet(List<BigDecimalWrapper> input, List<BigDecimalWrapper> output) {
        super(input, output);
        ensureNormalized(input);
        ensureNormalized(output);
    }

    private void ensureNormalized(List<BigDecimalWrapper> values) {
        Optional<BigDecimalWrapper> denormalized = values.stream().filter(value -> value.compareTo(BigDecimalWrapper.ZERO) < 0 || value.compareTo(BigDecimalWrapper.ONE) > 0)
                .findFirst();
        if(denormalized.isPresent()) {
            throw new IllegalArgumentException("One of the values is not normalized: " + values);
        }
    }

    public static List<NormalizedTrainingDataSet> asNormalized(List<TrainingDataSet> inputs) {
        normalize(inputs);
        return inputs.stream().map(NormalizedTrainingDataSet::new).collect(Collectors.toList());
    }

    public static <T extends NetworkInput> void normalize(List<T> inputs) {
        List<BigDecimalWrapper> maxValues = Lists.newArrayList(inputs.get(0).getInputValues());
        List<BigDecimalWrapper> minValues = Lists.newArrayList(inputs.get(0).getInputValues());
        for (NetworkInput input : inputs) {
            List<BigDecimalWrapper> row = input.getInputValues();
            for (int i = 0; i < row.size(); i++) {
                BigDecimalWrapper maxValue = maxValues.get(i);
                BigDecimalWrapper minValue = minValues.get(i);
                BigDecimalWrapper currentValue = row.get(i);
                if(maxValue.compareTo(currentValue) < 0) {
                    maxValues.set(i, currentValue);
                }
                if(minValue.compareTo(currentValue) > 0) {
                    minValues.set(i, currentValue);
                }
            }
        }
        inputs.stream()
                .forEach(input -> {
                    List<BigDecimalWrapper> row = input.getInputValues();
                    for (int i = 0; i < row.size(); i++) {
                        BigDecimalWrapper currentValue = row.get(i);
                        BigDecimalWrapper minValue = minValues.get(i);
                        BigDecimalWrapper maxValue = maxValues.get(i);
                        if(currentValue.equals(minValue) && maxValue.equals(minValue)) {
                            row.set(i, currentValue);
                        } else {
                            row.set(i, currentValue.subtract(minValue).divide(maxValue.subtract(minValue)));
                        }
                    }
                });
    }
}
