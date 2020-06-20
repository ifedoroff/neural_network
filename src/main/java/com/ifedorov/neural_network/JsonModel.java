package com.ifedorov.neural_network;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import java.io.*;
import java.math.BigDecimal;
import java.util.List;

public class JsonModel {

    private double learningFactor;
    private List<Tier> tiers;
    private int inputTierSize;

    public double getLearningFactor() {
        return learningFactor;
    }

    public void setLearningFactor(double learningFactor) {
        this.learningFactor = learningFactor;
    }

    public List<Tier> getTiers() {
        return tiers;
    }

    public void setTiers(List<Tier> tiers) {
        this.tiers = tiers;
    }

    public int getInputTierSize() {
        return inputTierSize;
    }

    public void setInputTierSize(int inputTierSize) {
        this.inputTierSize = inputTierSize;
    }

    public static class Tier {
        private List<Neuron> neurons;
        private List<Connection> connections;

        public List<Neuron> getNeurons() {
            return neurons;
        }

        public void setNeurons(List<Neuron> neurons) {
            this.neurons = neurons;
        }

        public List<Connection> getConnections() {
            return connections;
        }

        public void setConnections(List<Connection> connections) {
            this.connections = connections;
        }

        public Connection connectionBetween(int source, int target) {
            for (Connection connection : connections) {
                if(connection.source == source && connection.target == target) {
                    return connection;
                }
            }
            return null;
        }
    }

    public static class Connection {
        private int source;
        private int target;
        private double weight;

        public int getSource() {
            return source;
        }

        public void setSource(int source) {
            this.source = source;
        }

        public int getTarget() {
            return target;
        }

        public void setTarget(int target) {
            this.target = target;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }
    }

    public static class Neuron {
        private boolean isDummy;
        private String activationFn;

        public boolean isDummy() {
            return isDummy;
        }

        public void setDummy(boolean dummy) {
            isDummy = dummy;
        }

        public String getActivationFn() {
            return activationFn;
        }

        public void setActivationFn(String activationFn) {
            this.activationFn = activationFn;
        }

        public com.ifedorov.neural_network.Neuron toNeuron() {
            ActivationFn fn;
            if(isDummy) {
                fn = new ActivationFn.Pseudo();
            } else {
                fn = ActivationFn.fromString(activationFn);
            }
            return new com.ifedorov.neural_network.Neuron(fn);
        }
    }

    public static JsonModel fromJson(InputStream is) {
        return new Gson().fromJson(new InputStreamReader(is), JsonModel.class);
    }

    public static JsonModel fromJson(File file) {
        try(FileReader fileReader = new FileReader(file)) {
            return new Gson().fromJson(fileReader, JsonModel.class);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to decode Network Model from json", e);
        }
    }

    public String toJson() {
        return new Gson().toJson(this, JsonModel.class);
    }

    public Model toModel() {
        ModelBuilder builder = new ModelBuilder();
        builder.expectedInputSize(inputTierSize).expectedOutputSize(tiers.get(tiers.size() - 1).neurons.size())
                .learningFactor(new BigDecimal(learningFactor));
        int previousTierSize = inputTierSize;
        for (Tier jsonTier : tiers) {
            int currentTierSize = jsonTier.neurons.size();
            ModelBuilder.Tier tier = builder.tier();
            jsonTier.neurons.forEach((neuron -> tier.neuron(neuron.toNeuron())));
            WeightMatrix.Builder weightBuilder = new WeightMatrix.Builder(previousTierSize, currentTierSize);
            for (int i = 0; i < previousTierSize; i++) {
                List<BigDecimal> weights = Lists.newArrayList();
                for (int j = 0; j < currentTierSize; j++) {
                    Connection connection = jsonTier.connectionBetween(i, j);
                    if(connection != null) {
                        weights.add(BigDecimal.valueOf(connection.weight));
                    } else {
                        weights.add(null);
                    }
                }
                weightBuilder.row(weights.toArray(new BigDecimal[0]));
            }
            tier.weights(weightBuilder.build()).build();
            previousTierSize = currentTierSize;
        }
        return builder.build();
    }

}
