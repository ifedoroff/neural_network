package com.ifedorov.neural_network.train.dataset;

import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import com.ifedorov.neural_network.train.BigDecimalWrapper;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PredictionDataSet implements NetworkInput {

    private final List<BigDecimalWrapper> input;
    private List<BigDecimalWrapper> output;

    public PredictionDataSet(List<BigDecimalWrapper> input) {
        this.input = input;
    }

    public List<BigDecimalWrapper> getOutput() {
        return output;
    }

    public void setOutput(List<BigDecimalWrapper> output) {
        this.output = output;
    }

    public static List<PredictionDataSet> loadFromXLSFile(InputStream is, int networkInputDimension) {

        try {
            try(XSSFWorkbook workbook = new XSSFWorkbook(is)) {
                return loadFromXLSFile(workbook, networkInputDimension);
            }

        } catch (IOException e) {
            throw new RuntimeException("Unable to open file with Training dataset");
        }
    }

    public static List<PredictionDataSet> loadFromXLSFile(Path path, int networkInputDimension) {

        try {
            try(XSSFWorkbook workbook = new XSSFWorkbook(OPCPackage.open(path.toFile(), PackageAccess.READ))) {
                return loadFromXLSFile(workbook, networkInputDimension);
            }

        } catch (InvalidFormatException |IOException e) {
            throw new RuntimeException("Unable to open file with Training dataset");
        }
    }

    public static List<PredictionDataSet> loadFromXLSFile(XSSFWorkbook workbook, int networkInputDimension) {
        List<PredictionDataSet> predictionDataSets = Lists.newArrayList();
        Sheet sheet = workbook.sheetIterator().next();
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            PredictionDataSet predictionDataSet = new PredictionDataSet(
                    IntStream.range(0, networkInputDimension).mapToObj(i -> new BigDecimalWrapper(row.getCell(i).getNumericCellValue()))
                    .collect(Collectors.toList()));
            Verify.verify(predictionDataSet.getInputValues().size() == networkInputDimension);
            predictionDataSets.add(predictionDataSet);
        }
        return predictionDataSets;
    }

    @Override
    public List<BigDecimalWrapper> getInputValues() {
        return input;
    }
}
