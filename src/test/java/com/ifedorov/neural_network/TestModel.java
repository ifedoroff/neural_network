package com.ifedorov.neural_network;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestModel {

  @Test
  void testSaveAndLoad() throws IOException, InterruptedException {
    Model model = new ModelBuilder()
            .learningFactor(BigDecimal.valueOf(0.9))
            .tier()
            .neuron(new Neuron(new ActivationFn.Sigmoid(new BigDecimalWrapper(0.9))))
            .neuron(new Neuron(new ActivationFn.Sigmoid(new BigDecimalWrapper(0.9))))
            .weights(
                    new WeightMatrix.Builder()
                            .row(new BigDecimal[]{new BigDecimal(0.6), new BigDecimal(0.3)})
                            .row(new BigDecimal[]{new BigDecimal(0.1), new BigDecimal(0.9)})
                            .build()
            )
            .build()
            .tier()
            .neuron(new Neuron(new ActivationFn.Linear(new BigDecimalWrapper(0.7))))
            .weights(
                    new WeightMatrix.Builder()
                            .row(new BigDecimal[]{new BigDecimal(0.3)})
                            .row(new BigDecimal[]{new BigDecimal(0.8)})
                            .build()
            )
            .build()
            .build();
    List<TrainingDataSet> trainingDataSets = new ArrayList<>();
    trainingDataSets.add(new TrainingDataSet(Arrays.asList(BigDecimalWrapper.ZERO, BigDecimalWrapper.ZERO), Arrays.asList(BigDecimalWrapper.ONE)));
    trainingDataSets.add(new TrainingDataSet(Arrays.asList(BigDecimalWrapper.ZERO, BigDecimalWrapper.ONE), Arrays.asList(BigDecimalWrapper.ONE)));
    trainingDataSets.add(new TrainingDataSet(Arrays.asList(BigDecimalWrapper.ONE, BigDecimalWrapper.ZERO), Arrays.asList(BigDecimalWrapper.ONE)));
    trainingDataSets.add(new TrainingDataSet(Arrays.asList(BigDecimalWrapper.ONE, BigDecimalWrapper.ONE), Arrays.asList(BigDecimalWrapper.ZERO)));
    int epoch = 0;
    BigDecimalWrapper requiredAccuracy= new BigDecimalWrapper(0.000001);
    BigDecimalWrapper accuracy = BigDecimalWrapper.ONE.add(requiredAccuracy);
    while(epoch < 1000000 && accuracy.compareTo(requiredAccuracy) > 0) {
      accuracy = BigDecimalWrapper.ZERO;
      for (TrainingDataSet trainingDataSet : trainingDataSets) {
        BigDecimalWrapper currentError = model.train(trainingDataSet);
        accuracy = accuracy.add(currentError);
      }
      accuracy = accuracy.divide(new BigDecimalWrapper(trainingDataSets.size()));
      epoch++;
    }
    model.printState();

    List<BigDecimalWrapper> output1 = model.predict(new PredictionDataSet(Arrays.asList(BigDecimalWrapper.ZERO, BigDecimalWrapper.ZERO))).getOutput();
    List<BigDecimalWrapper> output2 = model.predict(new PredictionDataSet(Arrays.asList(BigDecimalWrapper.ZERO, BigDecimalWrapper.ONE))).getOutput();
    List<BigDecimalWrapper> output3 = model.predict(new PredictionDataSet(Arrays.asList(BigDecimalWrapper.ONE, BigDecimalWrapper.ZERO))).getOutput();
    List<BigDecimalWrapper> output4 = model.predict(new PredictionDataSet(Arrays.asList(BigDecimalWrapper.ONE, BigDecimalWrapper.ONE))).getOutput();

    Path modelFilePath = Paths.get(System.getProperty("user.dir")).resolve("build/test_model.xlsx");
    model.saveTo(modelFilePath);

    model = Model.load(modelFilePath);
    Assertions.assertIterableEquals(output1, model.predict(new PredictionDataSet(Arrays.asList(BigDecimalWrapper.ZERO, BigDecimalWrapper.ZERO))).getOutput());
    Assertions.assertIterableEquals(output2, model.predict(new PredictionDataSet(Arrays.asList(BigDecimalWrapper.ZERO, BigDecimalWrapper.ONE))).getOutput());
    Assertions.assertIterableEquals(output3, model.predict(new PredictionDataSet(Arrays.asList(BigDecimalWrapper.ONE, BigDecimalWrapper.ZERO))).getOutput());
    Assertions.assertIterableEquals(output4, model.predict(new PredictionDataSet(Arrays.asList(BigDecimalWrapper.ONE, BigDecimalWrapper.ONE))).getOutput());
  }

}
