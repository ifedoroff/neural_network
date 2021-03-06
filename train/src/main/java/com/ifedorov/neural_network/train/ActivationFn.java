package com.ifedorov.neural_network.train;

import com.google.common.base.Strings;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ActivationFn {

    Pattern PATTERN = Pattern.compile("(?<fnName>\\w+)(\\((?<alfa>.+?)\\))?", Pattern.CASE_INSENSITIVE);

    public static ActivationFn fromString(String str) {
        Matcher matcher = PATTERN.matcher(str);
        if(matcher.find()) {
            String name = matcher.group("fnName");
            String alfaString = matcher.group("alfa");
            Double alfa;
            if(Strings.isNullOrEmpty(alfaString)) {
                alfa = null;
            } else {
                alfa = Double.valueOf(alfaString);
            }
            Class<? extends ActivationFn> fnClass = null;
            switch (name) {
                case "lin":
                    fnClass = Linear.class;
                    break;
                case "sig":
                    fnClass = Sigmoid.class;
                    break;
                case "tan":
                    fnClass = Tangent.class;
                    break;
                case "pseudo":
                    fnClass = Pseudo.class;
                    break;
                default:
                    throw new IllegalArgumentException("Unable to create Neuron from specification : " + str);
            }

            try {
                if(alfa == null) {
                    return fnClass.getConstructor().newInstance();
                } else {
                    return fnClass.getConstructor(BigDecimalWrapper.class).newInstance(new BigDecimalWrapper(alfa));
                }
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Unable to create Neuron from specification : " + str, e);
            }
        }
        throw new IllegalArgumentException("Unable to create Neuron from specification : " + str);
    }

    BigDecimalWrapper calculate(BigDecimalWrapper input);

    BigDecimalWrapper derivative(BigDecimalWrapper input);

    BigDecimalWrapper getAlfa();

    class Linear implements ActivationFn {

        private BigDecimalWrapper alfa;

        public Linear(BigDecimalWrapper alfa) {
            this.alfa = alfa;
        }

        @Override
        public BigDecimalWrapper calculate(BigDecimalWrapper input) {
            return alfa.multiply(input);
        }

        @Override
        public BigDecimalWrapper derivative(BigDecimalWrapper fnResult) {
            return alfa;
        }

        @Override
        public BigDecimalWrapper getAlfa() {
            return alfa;
        }

        @Override
        public String toString() {
            return String.format(Locale.US, "lin(%.2f)", alfa.bigDecimal());
        }
    }

    public static class Sigmoid implements ActivationFn {

        private BigDecimalWrapper alfa;

        public Sigmoid(BigDecimalWrapper alfa) {
            this.alfa = alfa;
        }

        @Override
        public BigDecimalWrapper calculate(BigDecimalWrapper input) {
            try {
                return
                        BigDecimalWrapper.ONE.divide(
                                BigDecimalWrapper.ONE.add(
                                        input.multiply(alfa).negate().exp()
                                )
                        );
            } catch (NumberFormatException e) {
                throw e;
            }
        }

        @Override
        public BigDecimalWrapper derivative(BigDecimalWrapper fnResult) {
            return fnResult.multiply(BigDecimalWrapper.ONE.subtract(fnResult));
        }

        @Override
        public BigDecimalWrapper getAlfa() {
            return alfa;
        }

        @Override
        public String toString() {
            return String.format(Locale.US, "sig(%4.2f)", alfa.bigDecimal());
        }
    }

    public static class Tangent implements ActivationFn {

        private BigDecimalWrapper alfa;

        public Tangent(BigDecimalWrapper alfa) {
            this.alfa = alfa;
        }


        @Override
        public BigDecimalWrapper calculate(BigDecimalWrapper input) {
            BigDecimalWrapper numerator = input.divide(alfa).multiply(new BigDecimalWrapper(BigDecimal.valueOf(2))).exp()
                    .subtract(
                            BigDecimalWrapper.ONE
                    );
            BigDecimalWrapper denominator = input.divide(alfa).multiply(new BigDecimalWrapper(BigDecimal.valueOf(2))).exp()
                    .add(
                            BigDecimalWrapper.ONE
                    );
            return numerator.divide(denominator);
        }

        @Override
        public BigDecimalWrapper derivative(BigDecimalWrapper fnResult) {
            return BigDecimalWrapper.ONE.subtract(fnResult.pow(2));
        }

        @Override
        public BigDecimalWrapper getAlfa() {
            return alfa;
        }

        @Override
        public String toString() {
            return String.format(Locale.US, "tan(%.2f)", alfa.bigDecimal());
        }
    }

    /**
     * Effectively Linear with alfa = 1
     */
    class Pseudo implements ActivationFn {

        @Override
        public BigDecimalWrapper calculate(BigDecimalWrapper input) {
            return input;
        }

        @Override
        public BigDecimalWrapper derivative(BigDecimalWrapper input) {
            return BigDecimalWrapper.ONE;
        }

        @Override
        public BigDecimalWrapper getAlfa() {
            return BigDecimalWrapper.ZERO;
        }

        @Override
        public String toString() {
            return "pseudo()";
        }
    }
}