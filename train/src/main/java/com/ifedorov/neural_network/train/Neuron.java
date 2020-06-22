package com.ifedorov.neural_network.train;

import java.util.List;
import java.util.stream.IntStream;

public class Neuron {

    private final ActivationFn activationFn;
    private BigDecimalWrapper cachedActivated;

    public Neuron(ActivationFn activationFn) {
        this.activationFn = activationFn;
    }

    public ActivationFn getActivationFn() {
        return activationFn;
    }

    public BigDecimalWrapper calculate(List<BigDecimalWrapper> inputs, List<BigDecimalWrapper> weights) {
        BigDecimalWrapper sumOfInputSignals = calculateSum(inputs, weights);
        return (cachedActivated = activationFn.calculate(sumOfInputSignals));
    }

    public BigDecimalWrapper derivative() {
        return activationFn.derivative(cachedActivated);
    }

    private BigDecimalWrapper calculateSum(List<BigDecimalWrapper> inputs, List<BigDecimalWrapper> weights) {
        if(inputs.size() != weights.size()) {
            throw new IllegalArgumentException("Number of inputs should match number of weights");
        }
        return IntStream.range(0, inputs.size())
                .mapToObj(position -> {
                    BigDecimalWrapper weight = weights.get(position);
                    if(weight == null) {
                        return BigDecimalWrapper.ZERO;
                    } else {
                        return weight.multiply(inputs.get(position));
                    }
                })
                .reduce(BigDecimalWrapper.ZERO, BigDecimalWrapper::add);
    }

    public BigDecimalWrapper currentValue() {
        return cachedActivated;
    }

    @Override
    public String toString() {
        return activationFn.toString();
    }
}
