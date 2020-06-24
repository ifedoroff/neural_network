package com.ifedorov.neural_network.train;

import java.math.BigDecimal;

public interface StatisticWriter {

    void write(long epoch, BigDecimal accuracy, QualityCalculator.Quality calculatedQuality);
}
