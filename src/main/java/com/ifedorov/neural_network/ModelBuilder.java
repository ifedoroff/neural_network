package com.ifedorov.neural_network;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ModelBuilder {
    private BigDecimalWrapper learningFactor;
    private LinkedList<List<Neuron>> tiers = new LinkedList<>();
    private List<WeightMatrix> weightMatrices = new ArrayList<>();

    public Tier tier() {
        return new Tier();
    }

    public ModelBuilder learningFactor(BigDecimal learningFactor) {
        this.learningFactor = new BigDecimalWrapper(learningFactor);
        return this;
    }

    public Model build() {
        return new Model(tiers, weightMatrices, learningFactor);
    }

    public class Tier {

        private List<Neuron> neurons = new ArrayList<>();
        private WeightMatrix weights;
        public Tier neuron(Neuron neuron) {
            this.neurons.add(neuron);
            return this;
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
