package com.ifedorov.neural_network.train;

import java.math.BigDecimal;
import java.math.MathContext;

public class BigDecimalWrapper implements Comparable<BigDecimalWrapper> {

    private BigDecimal value;
    private MathContext mathContext;

    public static final BigDecimalWrapper ONE = new BigDecimalWrapper(BigDecimal.ONE, MathContext.DECIMAL64);
    public static final BigDecimalWrapper ZERO = new BigDecimalWrapper(BigDecimal.ZERO, MathContext.DECIMAL64);

    public BigDecimalWrapper(BigDecimal value) {
        this(value, null);
    }

    public BigDecimalWrapper(double value) {
        this(value, null);
    }

    public BigDecimalWrapper(BigDecimal value, MathContext mathContext) {
        this.value = value;
        this.mathContext = mathContext == null ? MathContext.DECIMAL64 : mathContext;
    }

    public BigDecimalWrapper(double value, MathContext mathContext) {
        this(new BigDecimal(value, mathContext == null ? (mathContext = MathContext.DECIMAL64) : mathContext), mathContext);
    }

    public BigDecimalWrapper add(BigDecimalWrapper bigDecimalWrapper) {
        return new BigDecimalWrapper(this.value.add(bigDecimalWrapper.value, mathContext), mathContext);
    }

    public BigDecimalWrapper subtract(BigDecimalWrapper bigDecimalWrapper) {
        return new BigDecimalWrapper(this.value.subtract(bigDecimalWrapper.value, mathContext), mathContext);
    }

    public BigDecimalWrapper multiply(BigDecimalWrapper bigDecimalWrapper) {
        return new BigDecimalWrapper(this.value.multiply(bigDecimalWrapper.value, mathContext), mathContext);
    }

    public BigDecimalWrapper divide(BigDecimalWrapper bigDecimalWrapper) {
        return new BigDecimalWrapper(this.value.divide(bigDecimalWrapper.value, mathContext), mathContext);
    }

    public BigDecimalWrapper pow(int power) {
        return new BigDecimalWrapper(this.value.pow(power, mathContext), mathContext);
    }

    public BigDecimalWrapper exp() {
        return new BigDecimalWrapper(Math.exp(value.doubleValue()), mathContext);
    }

    public BigDecimal bigDecimal() {
        return value;
    }

    public BigDecimalWrapper negate() {
        return new BigDecimalWrapper(value.negate(), mathContext);
    }

    @Override
    public int compareTo(BigDecimalWrapper o) {
        return this.value.compareTo(o.value);
    }

    public boolean isZero() {
        return value.doubleValue() == 0.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BigDecimalWrapper that = (BigDecimalWrapper) o;
        return value.equals(that.value);
    }

    @Override
    public String toString() {
        return bigDecimal().toString();
    }
}
