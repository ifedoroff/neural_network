package com.ifedorov.neural_network;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Neuron {

    public static final class Linear implements Function<BigDecimal, BigDecimal> {

        private BigDecimal alfa;

        public Linear(BigDecimal alfa) {
            this.alfa = alfa;
        }

        @Override
        public BigDecimal apply(BigDecimal bigDecimal) {
            return bigDecimal.multiply(alfa);
        }
    };

    public static final class Sigmoid implements Function<BigDecimal, BigDecimal> {

        private BigDecimal alfa;

        public Sigmoid(BigDecimal alfa) {
            this.alfa = alfa;
        }

        @Override
        public BigDecimal apply(BigDecimal bigDecimal) {
            return
                    BigDecimal.ONE.divide(
                            BigDecimal.ONE.add(
                                BigDecimal.valueOf(Math.exp(-1 * bigDecimal.doubleValue() * alfa.doubleValue()))
                            )
                    );
        }
    };

    public static final Function<BigDecimal, BigDecimal> SIGMOID = new Function<BigDecimal, BigDecimal>() {
        @Override
        public BigDecimal apply(BigDecimal bigDecimal) {
            return null;
        }
    };

    private final Function<BigDecimal, BigDecimal> activationFn;
    private BigDecimal cachedValue;

    public Neuron(Function<BigDecimal, BigDecimal> activationFn) {
        this.activationFn = activationFn;
    }

    public BigDecimal calculate(List<BigDecimal> inputs, List<BigDecimal> weights) {
        if(cachedValue == null) {
            return (cachedValue = activationFn.apply(calculateSum(inputs, weights)));
        } else {
            return cachedValue;
        }
    }

    private BigDecimal calculateSum(List<BigDecimal> inputs, List<BigDecimal> weights) {
        if(inputs.size() != weights.size()) {
            throw new IllegalArgumentException("Number of inputs should match number of weights");
        }
        return IntStream.range(0, inputs.size())
                .mapToObj(position -> weights.get(position).multiply(inputs.get(position)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal currentValue() {
        return cachedValue;
    }

}
