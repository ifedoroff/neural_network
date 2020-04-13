package com.ifedorov.neural_network;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Model {

    private BigDecimal learningFactor;
    private LinkedList<List<Neuron>> tiers = new LinkedList<>();
    private List<WeightMatrix> weightMatrices;

    public Model(List<List<Neuron>> tiers, List<WeightMatrix> weightMatrices, BigDecimal learningFactor) {
        this.learningFactor = learningFactor;
        if(tiers.size() != weightMatrices.size())
            throw new IllegalArgumentException("Number of Tiers should be equals to number of Weight matrices");
        this.tiers = new LinkedList<>(tiers);
        this.weightMatrices = weightMatrices;
    }

    public BigDecimal train(TrainingDataSet trainingDataSet) {
        if(trainingDataSet.input.size() != tiers.getFirst().size())
            throw new IllegalArgumentException("Number of input values should be equals to the number of Neurons of the first level");
        if(trainingDataSet.output.size() != tiers.getLast().size())
            throw new IllegalArgumentException("Number of input values should be equals to the number of Neurons of the first level");
        forwardPass(trainingDataSet.input);
        backwardPass(trainingDataSet.output);
        return calculateAccuracy(trainingDataSet.output);
    }

    private BigDecimal calculateAccuracy(List<BigDecimal> expectedOutputs) {
        List<Neuron> outputTier = tiers.getLast();
        return IntStream.range(0, outputTier.size())
                .mapToObj(index -> outputTier.get(index).currentValue().min(expectedOutputs.get(index)).pow(2))
                .reduce(BigDecimal::add).get();
    }

    private void backwardPass(List<BigDecimal> expectedOutput) {
        List<BigDecimal> higherLevelError = calculateErrorForOutputTier(expectedOutput);
        for (int i = tiers.size() - 2; i >= 0; i--) {
            List<Neuron> currentTier = tiers.get(i);
            List<Neuron> higherTier = tiers.get(i + 1);
            WeightMatrix currentToHigherTierWeightMatrix = weightMatrices.get(i + 1);
            List<BigDecimal> finalHigherLevelError = higherLevelError;
            List<BigDecimal> levelError = IntStream.range(0, currentTier.size())
                    .mapToObj(neuronIndex -> {
                        BigDecimal neuronValue = currentTier.get(neuronIndex).currentValue();
                        List<BigDecimal> weights = currentToHigherTierWeightMatrix.getWeightForNeuron(neuronIndex);
                        return IntStream.range(0, weights.size())
                                .mapToObj(weightIndex -> weights.get(weightIndex).multiply(finalHigherLevelError.get(weightIndex)))
                                .reduce(BigDecimal::add)
                                .get().multiply(BigDecimal.ONE.min(neuronValue).multiply(neuronValue));
                    }).collect(Collectors.toList());
            WeightMatrix higherToCurrentTierWeightMatrix = weightMatrices.get(i + 1).transposed();
            List<BigDecimal> finalHigherLevelError1 = higherLevelError;
            IntStream.range(0, higherTier.size())
                    .forEach(higherNeuronIndex -> {
                        higherToCurrentTierWeightMatrix.walk((row, column, value) -> value.add(learningFactor.multiply(finalHigherLevelError1.get(row)).multiply(currentTier.get(column).currentValue())));
                    });
            higherLevelError = levelError;
        }
    }

    private List<BigDecimal> calculateErrorForOutputTier(List<BigDecimal> expectedOutput) {
        List<Neuron> outputTier = tiers.getLast();
        return IntStream.range(0, expectedOutput.size())
                .mapToObj(index -> {
                    BigDecimal actualValue = outputTier.get(index).currentValue();
                    BigDecimal expectedValue = expectedOutput.get(index);
                    return (expectedValue.min(actualValue).multiply(BigDecimal.ONE.min(actualValue)));
                }).collect(Collectors.toList());
    }

    private void forwardPass(List<BigDecimal> inputs) {
        for (int i = 0; i < tiers.size(); i++) {
            final int tierLevel = i;
            List<Neuron> tierNeurons = tiers.get(tierLevel);
            List<BigDecimal> finalInputs = inputs;
            inputs = IntStream.range(0, tierNeurons.size())
                    .mapToObj(neuronPosition -> {
                        WeightMatrix weightMatrix = weightMatrices.get(tierLevel);
                        return tierNeurons.get(neuronPosition).calculate(finalInputs, weightMatrix.transposed().getWeightForNeuron(neuronPosition));
                    }).collect(Collectors.toList());
        }
    }


}
