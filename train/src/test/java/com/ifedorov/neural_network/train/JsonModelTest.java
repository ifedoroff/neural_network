package com.ifedorov.neural_network.train;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonModelTest {

    @Test
    void testRead() {
        JsonModel model = JsonModel.fromJson(getClass().getClassLoader().getResourceAsStream("model.json"));
        assertEquals(0.9, model.getLearningFactor());
        assertEquals(3, model.getInputTierSize());
        assertEquals(2, model.getTiers().size());
        JsonModel.Tier firstTier = model.getTiers().get(0);
        assertEquals(5, firstTier.getNeurons().size());
        assertEquals(true, firstTier.getNeurons().get(0).isDummy());
        assertEquals(true, firstTier.getNeurons().get(1).isDummy());
        assertEquals(false, firstTier.getNeurons().get(2).isDummy());
        assertEquals(false, firstTier.getNeurons().get(3).isDummy());
        assertEquals(false, firstTier.getNeurons().get(4).isDummy());
        assertEquals(6, firstTier.getConnections().size());
        assertEquals(0.75, firstTier.connectionBetween(0, 0).getWeight());
        assertEquals(1, firstTier.connectionBetween(1, 0).getWeight());
        assertEquals(0.15, firstTier.connectionBetween(0, 1).getWeight());
        assertEquals(0.3, firstTier.connectionBetween(0, 2).getWeight());
        assertEquals(0.5, firstTier.connectionBetween(1, 3).getWeight());
        assertEquals(0.9, firstTier.connectionBetween(2, 4).getWeight());
        JsonModel.Tier secondTier = model.getTiers().get(1);
        assertEquals(2, secondTier.getNeurons().size());
        assertEquals(false, secondTier.getNeurons().get(0).isDummy());
        assertEquals(false, secondTier.getNeurons().get(1).isDummy());
        assertEquals(5, secondTier.getConnections().size());
        assertEquals(0.1, secondTier.connectionBetween(0, 0).getWeight());
        assertEquals(0.5, secondTier.connectionBetween(1, 0).getWeight());
        assertEquals(1, secondTier.connectionBetween(4, 0).getWeight());
        assertEquals(1, secondTier.connectionBetween(2, 1).getWeight());
        assertEquals(1, secondTier.connectionBetween(3, 1).getWeight());
    }

    @Test
    void testToModel() {
        JsonModel jsonModel = JsonModel.fromJson(getClass().getClassLoader().getResourceAsStream("model.json"));
        checkModel(jsonModel);
    }

    @Test
    void testFromToModel() {
        Model model = JsonModel.fromJson(getClass().getClassLoader().getResourceAsStream("model.json")).toModel();
        JsonModel jsonModel = model.toJsonModel();
        checkModel(jsonModel);
    }

    private void checkModel(JsonModel jsonModel) {
        Model model = jsonModel.toModel();
        assertEquals(3, model.getInputDimension());
        assertEquals(2, model.getOutputDimension());
        assertEquals(0.9, model.getLearningFactor().bigDecimal().doubleValue());
        List<Neuron> firstTier = model.tier(0);

        assertEquals(5, firstTier.size());
        assertEquals(ActivationFn.Pseudo.class, firstTier.get(0).getActivationFn().getClass());
        assertEquals(ActivationFn.Pseudo.class, firstTier.get(1).getActivationFn().getClass());
        assertEquals(ActivationFn.Sigmoid.class, firstTier.get(2).getActivationFn().getClass());
        assertEquals(0.5, firstTier.get(2).getActivationFn().getAlfa().bigDecimal().doubleValue());
        assertEquals(ActivationFn.Linear.class, firstTier.get(3).getActivationFn().getClass());
        assertEquals(0.3, firstTier.get(3).getActivationFn().getAlfa().bigDecimal().doubleValue());
        assertEquals(ActivationFn.Tangent.class, firstTier.get(4).getActivationFn().getClass());
        assertEquals(0.5, firstTier.get(4).getActivationFn().getAlfa().bigDecimal().doubleValue());

        WeightMatrix firstTierWeights = model.weightsForLevel(0);
        assertEquals(0.75, firstTierWeights.getWeightForNeuron(0).get(0).bigDecimal().doubleValue());
        assertEquals(1, firstTierWeights.getWeightForNeuron(1).get(0).bigDecimal().doubleValue());
        assertEquals(0.15, firstTierWeights.getWeightForNeuron(0).get(1).bigDecimal().doubleValue());
        assertEquals(0.3, firstTierWeights.getWeightForNeuron(0).get(2).bigDecimal().doubleValue());
        assertEquals(0.5, firstTierWeights.getWeightForNeuron(1).get(3).bigDecimal().doubleValue());
        assertEquals(0.9, firstTierWeights.getWeightForNeuron(2).get(4).bigDecimal().doubleValue());


        List<Neuron> secondTier = model.tier(1);
        assertEquals(2, secondTier.size());

        WeightMatrix secondTierWeights = model.weightsForLevel(1);
        assertEquals(0.1, secondTierWeights.getWeightForNeuron(0).get(0).bigDecimal().doubleValue());
        assertEquals(0.5, secondTierWeights.getWeightForNeuron(1).get(0).bigDecimal().doubleValue());
        assertEquals(1, secondTierWeights.getWeightForNeuron(4).get(0).bigDecimal().doubleValue());
        assertEquals(1, secondTierWeights.getWeightForNeuron(2).get(1).bigDecimal().doubleValue());
        assertEquals(1, secondTierWeights.getWeightForNeuron(3).get(1).bigDecimal().doubleValue());
    }

}