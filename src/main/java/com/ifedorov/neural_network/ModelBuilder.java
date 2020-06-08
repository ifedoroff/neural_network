package com.ifedorov.neural_network;

import com.google.common.base.Verify;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ModelBuilder {
    private BigDecimalWrapper learningFactor;
    private LinkedList<List<Neuron>> tiers = new LinkedList<>();
    private List<WeightMatrix> weightMatrices = new ArrayList<>();
    private int expectedInputSize;
    private int expectedOutputSize;

    public Tier tier() {
        return new Tier();
    }

    public ModelBuilder learningFactor(BigDecimal learningFactor) {
        this.learningFactor = new BigDecimalWrapper(learningFactor);
        return this;
    }

    public ModelBuilder expectedInputSize(int expectedInputSize) {
        this.expectedInputSize = expectedInputSize;
        return this;
    }

    public ModelBuilder expectedOutputSize(int expectedOutputSize) {
        this.expectedOutputSize = expectedOutputSize;
        return this;
    }

    public Model build() {
        Model model = new Model(tiers, weightMatrices, learningFactor);
        Verify.verify(model.getInputDimension() == expectedInputSize, "Incorrect model configuration, wrong input size: expected %s, actual %s", expectedInputSize, model.getInputDimension());
        Verify.verify(model.getOutputDimension() == expectedOutputSize, "Incorrect model configuration, wrong output size: expected %s, actual %s", expectedOutputSize, model.getOutputDimension());
        return model;
    }

    public class Tier {

        private List<Neuron> neurons = new ArrayList<>();
        private WeightMatrix weights;
        public Tier neuron(Neuron neuron) {
            this.neurons.add(neuron);
            return this;
        }

        public int neuronCount() {
            return neurons.size();
        }

        public Tier weights(WeightMatrix weightMatrix) {
            this.weights = weightMatrix;
            return this;
        }

        public ModelBuilder build() {
            tiers.add(this.neurons);
            weightMatrices.add(weights);
            return ModelBuilder.this;
        }

    }
}
