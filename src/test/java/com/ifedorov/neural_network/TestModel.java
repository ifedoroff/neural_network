package com.ifedorov.neural_network;

import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.apache.commons.math3.util.BigReal;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TestModel {

  @Test
  void test() {
    List<List<Neuron>> tiers = new ArrayList<List<Neuron>>();
    tiers.add(Arrays.asList(new Neuron(new Neuron.Sigmoid(BigDecimal.valueOf(0.9))),
            new Neuron(new Neuron.Sigmoid(BigDecimal.valueOf(0.9)))));
    tiers.add(Arrays.asList(new Neuron(new Neuron.Linear(BigDecimal.valueOf(0.7)))));
    List<WeightMatrix> weightMatrices = new ArrayList<>();
    weightMatrices.add(new WeightMatrix(new Array2DRowFieldMatrix<BigReal>(new BigReal[][] {
                    {new BigReal(0.6), new BigReal(0.9)},
                    {new BigReal(0.1), new BigReal(0.5)}
            })));
    weightMatrices.add(new WeightMatrix(new Array2DRowFieldMatrix<BigReal>(new BigReal[][] {
            {new BigReal(0.3), new BigReal(0.8)}
    })));
    List<TrainingDataSet> dataSets = new ArrayList<>();
    dataSets.add(new TrainingDataSet(Arrays.asList(BigDecimal.ZERO, BigDecimal.ZERO), Arrays.asList(BigDecimal.ONE)));
    dataSets.add(new TrainingDataSet(Arrays.asList(BigDecimal.ZERO, BigDecimal.ONE), Arrays.asList(BigDecimal.ONE)));
    dataSets.add(new TrainingDataSet(Arrays.asList(BigDecimal.ONE, BigDecimal.ZERO), Arrays.asList(BigDecimal.ONE)));
    dataSets.add(new TrainingDataSet(Arrays.asList(BigDecimal.ONE, BigDecimal.ONE), Arrays.asList(BigDecimal.ZERO)));
    Model model = new Model(tiers, weightMatrices, BigDecimal.valueOf(0.7));
    int epochs = 10;
    BigDecimal accuracy = BigDecimal.ONE;
    BigDecimal requiredAccuracy= BigDecimal.valueOf(0.1);
    while(epochs > 0 && accuracy.compareTo(requiredAccuracy) > 0) {
      for (TrainingDataSet dataSet : dataSets) {
        accuracy = model.train(dataSet);
        if(accuracy.compareTo(requiredAccuracy) <= 0) {
          break;
        }
      }
      epochs--;
    }
    System.out.println(accuracy);
  }

}
