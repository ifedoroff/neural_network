package com.ifedorov.neural_network;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TestModel {

  @Test
  void testTrain() throws IOException, InterruptedException {
    Model model = new ModelBuilder()
            .learningFactor(BigDecimal.valueOf(0.9))
            .tier()
            .neuron(new Neuron(new ActivationFn.Sigmoid(new BigDecimalWrapper(0.9))))
            .neuron(new Neuron(new ActivationFn.Sigmoid(new BigDecimalWrapper(0.9))))
            .weights(
                    new WeightMatrix.Builder(2, 2)
                            .row(new BigDecimal[]{new BigDecimal(0.6), new BigDecimal(0.3)})
                            .row(new BigDecimal[]{new BigDecimal(0.1), new BigDecimal(0.9)})
                            .build()
            )
            .build()
            .tier()
            .neuron(new Neuron(new ActivationFn.Linear(new BigDecimalWrapper(0.7))))
            .weights(
                    new WeightMatrix.Builder(2, 2)
                            .row(new BigDecimal[]{new BigDecimal(0.3)})
                            .row(new BigDecimal[]{new BigDecimal(0.8)})
                            .build()
            )
            .build()
            .expectedOutputSize(1)
            .expectedInputSize(2)
            .build();
    List<NormalizedTrainingDataSet> trainingDataSets = new ArrayList<>();
    trainingDataSets.add(new NormalizedTrainingDataSet(Arrays.asList(BigDecimalWrapper.ZERO, BigDecimalWrapper.ZERO), Arrays.asList(BigDecimalWrapper.ONE)));
    trainingDataSets.add(new NormalizedTrainingDataSet(Arrays.asList(BigDecimalWrapper.ZERO, BigDecimalWrapper.ONE), Arrays.asList(BigDecimalWrapper.ONE)));
    trainingDataSets.add(new NormalizedTrainingDataSet(Arrays.asList(BigDecimalWrapper.ONE, BigDecimalWrapper.ZERO), Arrays.asList(BigDecimalWrapper.ONE)));
    trainingDataSets.add(new NormalizedTrainingDataSet(Arrays.asList(BigDecimalWrapper.ONE, BigDecimalWrapper.ONE), Arrays.asList(BigDecimalWrapper.ZERO)));
    train(model, trainingDataSets);
  }

  private void train(Model model, List<NormalizedTrainingDataSet> trainingDataSets) {
    int epoch = 0;
//    BigDecimalWrapper requiredAccuracy= new BigDecimalWrapper(0.000001);
//    BigDecimalWrapper accuracy = BigDecimalWrapper.ONE.add(requiredAccuracy);
//    while(epoch < 1000000 && accuracy.compareTo(requiredAccuracy) > 0) {
//      accuracy = BigDecimalWrapper.ZERO;
//      for (NormalizedTrainingDataSet trainingDataSet : trainingDataSets) {
//        BigDecimalWrapper currentError = model.train(trainingDataSet);
//        accuracy = accuracy.add(currentError);
//      }
//      accuracy = accuracy.divide(new BigDecimalWrapper(trainingDataSets.size()));
//      epoch++;
//    }
    QualityCalculator.Quality quality = new QualityCalculator.Quality(new BigDecimal(0.95), new BigDecimal(0.95), new BigDecimal(0.95), new BigDecimal(0.95));
    model.train(trainingDataSets, new BaseStopIndicator(1000000, 100000, trainingDataSets, quality, new BaseStopIndicator.Listener() {
      @Override
      public void statistics(long epoch, BigDecimal accuracy, QualityCalculator.Quality calculatedQuality) {

      }
    }));
    model.printState();
  }

  @Test
  void testSaveAndLoadXLS() throws IOException {
    try(InputStream modelStream = getClass().getClassLoader().getResourceAsStream("test_model.xlsx");) {
      Model model = Model.load(modelStream);
      Path modelFilePath = Paths.get(System.getProperty("user.dir")).resolve("build/test_model.xlsx");
      model.saveTo(modelFilePath);
      Model loaded = Model.load(modelFilePath);
      assertEquals(model.getInputDimension(), loaded.getInputDimension());
      assertEquals(model.getOutputDimension(), loaded.getOutputDimension());
      assertEquals(model.weights().size(), loaded.weights().size());
    }
  }

  @Test
  void testModelReader() throws IOException {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream("test_model_reader.xlsx")){
      Model model = Model.load(is);
      List<WeightMatrix> weights = model.weights();
      assertEquals(2, weights.size());
      assertEquals(4, weights.get(0).rowDimension());
      assertEquals(2, weights.get(0).columnDimension());
      assertEquals(2, weights.get(1).rowDimension());
      assertEquals(2, weights.get(1).columnDimension());
    }
  }

}
