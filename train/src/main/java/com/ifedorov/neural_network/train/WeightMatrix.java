package com.ifedorov.neural_network.train;

import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.apache.commons.math3.linear.FieldMatrixChangingVisitor;
import org.apache.commons.math3.util.BigReal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class WeightMatrix {
    private final Array2DRowFieldMatrix<BigReal> matrix;

    public WeightMatrix(Array2DRowFieldMatrix<BigReal> matrix) {
        this.matrix = matrix;
    }

    public List<BigDecimalWrapper> getWeightForNeuron(int neuronIndex) {
        return Arrays.stream(matrix.getRow(neuronIndex)).map(BigReal::bigDecimalValue).map(bigDecimal -> {
            if(bigDecimal == null) {
                return null;
            } else {
                return new BigDecimalWrapper(bigDecimal);
            }
        }).collect(Collectors.toList());
    }

    public WeightMatrix transposed() {
        return new WeightMatrix((Array2DRowFieldMatrix<BigReal>) matrix.transpose());
    }

    public int rowDimension() {
        return matrix.getRowDimension();
    }

    public int columnDimension() {
        return matrix.getColumnDimension();
    }

    public void walk(Visitor visitor) {
        matrix.walkInRowOrder(new FieldMatrixChangingVisitor<BigReal>() {
            @Override
            public void start(int rows, int columns, int startRow, int endRow, int startColumn, int endColumn) {

            }

            @Override
            public BigReal visit(int row, int column, BigReal value) {
                return new BigReal(value == null || value.bigDecimalValue() == null ? null : visitor.visit(row, column, new BigDecimalWrapper(value.bigDecimalValue())).bigDecimal());
            }

            @Override
            public BigReal end() {
                return null;
            }
        });
    }

    public List<BigDecimalWrapper> row(int num) {
        return Arrays.stream(matrix.getRow(num)).map(BigReal::bigDecimalValue).map(bigDecimal -> {
            if(bigDecimal == null) {
                return null;
            } else {
                return new BigDecimalWrapper(bigDecimal);
            }
        }).collect(Collectors.toList());
    }

    public Stream<List<BigDecimalWrapper>> rows() {
        return IntStream.range(0, matrix.getRowDimension())
                .mapToObj(this::row);
    }

    public interface Visitor {
        BigDecimalWrapper visit(int row, int column, BigDecimalWrapper value);
    }

    public void printState() {
        BigReal[][] data = matrix.getData();
        for (int i = 0; i < data.length; i++) {
            BigReal[] row = data[i];
            for (int j = 0; j < row.length; j++) {
                BigDecimal weight = row[j].bigDecimalValue();
                if(weight == null) {
                    System.out.println("    ");
                } else {
                    System.out.printf("%4.2f | ", weight);
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    public static class Builder {

        private final int rowDimension;
        private final int columnDimension;
        private List<BigDecimal[]> weights = new ArrayList<>();

        public Builder(int rowDimension, int columnDimension) {

            this.rowDimension = rowDimension;
            this.columnDimension = columnDimension;
        }

        public Builder row(BigDecimal[] row) {
            weights.add(row);
            return this;
        }

        public WeightMatrix build() {
            int rowsMissing = rowDimension - weights.size();
            for (int i = 0; i < rowsMissing; i++) {
                List<BigDecimal> row = IntStream.range(0, columnDimension).mapToObj((x) -> BigDecimal.ZERO).collect(Collectors.toList());
                weights.add(
                        row.toArray(new BigDecimal[0])
                );
            }

            return new WeightMatrix(new Array2DRowFieldMatrix<BigReal>(weights.stream()
                    .map(bigDecimals -> Arrays.stream(bigDecimals).map(BigReal::new).collect(Collectors.toList()))
                    .map(bigDecimals -> bigDecimals.toArray(new BigReal[0]))
                    .collect(Collectors.toList()).toArray(new BigReal[0][])));
        }
    }
}
