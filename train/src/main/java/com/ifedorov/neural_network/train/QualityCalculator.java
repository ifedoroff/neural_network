package com.ifedorov.neural_network.train;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.ResourceBundle;

public class QualityCalculator {

    public static final ResourceBundle localization = Utf8ResourceBundle.getBundle("Messages");

    public Quality calculateQuality(List<BigDecimalWrapper> expectedOutputs, List<BigDecimalWrapper> actualOutputs) {
        BigDecimal truePositive = truePositive(expectedOutputs, actualOutputs);
        BigDecimal trueNegative = trueNegative(expectedOutputs, actualOutputs);
        BigDecimal falsePositive = falsePositive(expectedOutputs, actualOutputs);
        BigDecimal falseNegative = falseNegative(expectedOutputs, actualOutputs);
        return new Quality(
                accuracy(truePositive, falsePositive),
                adequacy(truePositive, falseNegative),
                specificity(falsePositive, trueNegative),
                averageAdequacyAndAccuracy(truePositive, falsePositive, falseNegative)
        );
    }

    public static BigDecimal accuracy(BigDecimal truePositive, BigDecimal falsePositive) {
        if(BigDecimal.ZERO.compareTo(truePositive) == 0 && BigDecimal.ZERO.compareTo(falsePositive) == 0) {
            return BigDecimal.ZERO;
        }
        return truePositive.divide(truePositive.add(falsePositive), MathContext.DECIMAL32);
    }

    public static BigDecimal accuracy(List<BigDecimalWrapper> expectedOutputs, List<BigDecimalWrapper> actualOutputs) {
        BigDecimal truePositive = truePositive(expectedOutputs, actualOutputs);
        BigDecimal falsePositive = falsePositive(expectedOutputs, actualOutputs);
        return accuracy(truePositive, falsePositive);
    }

    public static BigDecimal adequacy(BigDecimal truePositive, BigDecimal falseNegative) {
        if(BigDecimal.ZERO.compareTo(truePositive) == 0 && BigDecimal.ZERO.compareTo(falseNegative) == 0) {
            return BigDecimal.ZERO;
        }
        return truePositive.divide(truePositive.add(falseNegative), MathContext.DECIMAL32);
    }

    public static BigDecimal adequacy(List<BigDecimalWrapper> expectedOutputs, List<BigDecimalWrapper> actualOutputs) {
        BigDecimal truePositive = truePositive(expectedOutputs, actualOutputs);
        BigDecimal falseNegative = falseNegative(expectedOutputs, actualOutputs);
        return adequacy(truePositive, falseNegative);
    }

    public static BigDecimal specificity(List<BigDecimalWrapper> expectedOutputs, List<BigDecimalWrapper> actualOutputs) {
        BigDecimal falsePositive = falsePositive(expectedOutputs, actualOutputs);
        BigDecimal trueNegative = trueNegative(expectedOutputs, actualOutputs);
        return specificity(falsePositive, trueNegative);
    }

    public static BigDecimal specificity(BigDecimal falsePositive, BigDecimal trueNegative) {
        if(BigDecimal.ZERO.compareTo(falsePositive) == 0 && BigDecimal.ZERO.compareTo(trueNegative) == 0) {
            return BigDecimal.ZERO;
        }
        return trueNegative.divide(trueNegative.add(falsePositive), MathContext.DECIMAL32);
    }

    public static BigDecimal averageAdequacyAndAccuracy(List<BigDecimalWrapper> expectedOutputs, List<BigDecimalWrapper> actualOutputs) {
        BigDecimal truePositive = truePositive(expectedOutputs, actualOutputs);
        BigDecimal falsePositive = falsePositive(expectedOutputs, actualOutputs);
        BigDecimal falseNegative = falseNegative(expectedOutputs, actualOutputs);
        return averageAdequacyAndAccuracy(truePositive, falsePositive, falseNegative);
    }

    public static BigDecimal averageAdequacyAndAccuracy(BigDecimal truePositive, BigDecimal falsePositive, BigDecimal falseNegative) {
        return new BigDecimal(2, MathContext.DECIMAL32).multiply(truePositive, MathContext.DECIMAL32)
                .divide(
                        new BigDecimal(2).multiply(truePositive, MathContext.DECIMAL32).add(falsePositive).add(falseNegative),
                        MathContext.DECIMAL32
                );
    }

    public static BigDecimal truePositive(List<BigDecimalWrapper> expectedOutput, List<BigDecimalWrapper> actualOutput) {
        int numberOfTruePositives = 0;
        for (int i = 0; i < expectedOutput.size(); i++) {

            if(round(expectedOutput.get(i)) == 1) {
                if(round(actualOutput.get(i)) == 1) {
                    numberOfTruePositives++;
                } else {
                    System.out.println(round(actualOutput.get(i)) + ": " + actualOutput.get(i).bigDecimal());
                }
            }
        }
        return new BigDecimal((double)numberOfTruePositives / (double) expectedOutput.size(), MathContext.DECIMAL32);
    }

    public static BigDecimal falsePositive(List<BigDecimalWrapper> expectedOutput, List<BigDecimalWrapper> actualOutput) {
        int numberOfFalsePositives = 0;
        for (int i = 0; i < expectedOutput.size(); i++) {
            if(round(expectedOutput.get(i)) == 0 && round(actualOutput.get(i)) == 1) {
                numberOfFalsePositives++;
            }
        }
        return new BigDecimal((double)numberOfFalsePositives / (double) expectedOutput.size(), MathContext.DECIMAL32);
    }

    public static BigDecimal falseNegative(List<BigDecimalWrapper> expectedOutput, List<BigDecimalWrapper> actualOutput) {
        int numberOfFalseNegatives = 0;
        for (int i = 0; i < expectedOutput.size(); i++) {
            if(round(expectedOutput.get(i)) == 1 && round(actualOutput.get(i)) == 0) {
                numberOfFalseNegatives++;
            }
        }
        return new BigDecimal((double)numberOfFalseNegatives / (double) expectedOutput.size(), MathContext.DECIMAL32);
    }

    public static BigDecimal trueNegative(List<BigDecimalWrapper> expectedOutput, List<BigDecimalWrapper> actualOutput) {
        int numberOfTrueNegatives = 0;
        for (int i = 0; i < expectedOutput.size(); i++) {

            if(round(expectedOutput.get(i)) == 0 && round(actualOutput.get(i)) == 0) {
                numberOfTrueNegatives++;
            }
        }
        return new BigDecimal((double)numberOfTrueNegatives / (double) expectedOutput.size(), MathContext.DECIMAL32);
    }

    private static int round(BigDecimalWrapper decimal) {
        BigDecimal abs = decimal.bigDecimal().abs();
        if(abs.compareTo(BigDecimal.ONE.subtract(abs)) < 0) {
            return 0;
        } else {
            return 1;
        }
    }

    public static class Quality{
        public final BigDecimal accuracy;
        public final BigDecimal adequacy;
        public final BigDecimal specificity;
        public final BigDecimal average;

        public Quality(BigDecimal accuracy, BigDecimal adequacy, BigDecimal specificity, BigDecimal average) {
            this.accuracy = accuracy;
            this.adequacy = adequacy;
            this.specificity = specificity;
            this.average = average;
        }

        @Override
        public String toString() {
            return localization.getString("quality") + " {" +
                    localization.getString("accuracy") + "=" + accuracy +
                    ", " + localization.getString("adequacy") + "=" + adequacy +
                    ", " + localization.getString("specificity") + "=" + specificity +
                    ", " + localization.getString("average") + "=" + average +
                    '}';
        }
    }

}
