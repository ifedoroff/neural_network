package com.ifedorov.neural_network;

import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.apache.commons.math3.linear.FieldMatrixChangingVisitor;
import org.apache.commons.math3.util.BigReal;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WeightMatrix {
    private final Array2DRowFieldMatrix<BigReal> matrix;

    public WeightMatrix(Array2DRowFieldMatrix<BigReal> matrix) {
        this.matrix = matrix;
    }

    public List<BigDecimal> getWeightForNeuron(int neuronIndex) {
        return Arrays.stream(matrix.getColumn(neuronIndex)).map(BigReal::bigDecimalValue).collect(Collectors.toList());
    }

    public WeightMatrix transposed() {
        return new WeightMatrix((Array2DRowFieldMatrix<BigReal>) matrix.transpose());
    }

    public void walk(Visitor visitor) {
        matrix.walkInRowOrder(new FieldMatrixChangingVisitor<BigReal>() {
            @Override
            public void start(int rows, int columns, int startRow, int endRow, int startColumn, int endColumn) {

            }

            @Override
            public BigReal visit(int row, int column, BigReal value) {
                return new BigReal(visitor.visit(row, column, value.bigDecimalValue()));
            }

            @Override
            public BigReal end() {
                return null;
            }
        });
    }

    public interface Visitor {
        BigDecimal visit(int row, int column, BigDecimal value);
    }
}
