package com.ifedorov.neural_network;

import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class PredictionDataSet {

    public final List<BigDecimalWrapper> input;
    private List<BigDecimalWrapper> output;

    public PredictionDataSet(List<BigDecimalWrapper> input) {
        this.input = input;
    }

    public List<BigDecimalWrapper> getInput() {
        return input;
    }

    public List<BigDecimalWrapper> getOutput() {
        return output;
    }

    public void setOutput(List<BigDecimalWrapper> output) {
        this.output = output;
    }

    public static List<PredictionDataSet> loadFromXLSFile(InputStream is, int inputSize) {

        try {
            try(XSSFWorkbook workbook = new XSSFWorkbook(is)) {
                return loadFromXLSFile(workbook, inputSize);
            }

        } catch (IOException e) {
            throw new RuntimeException("Unable to open file with Training dataset");
        }
    }

    public static List<PredictionDataSet> loadFromXLSFile(Path path, int inputSize) {

        try {
            try(XSSFWorkbook workbook = new XSSFWorkbook(path.toFile())) {
                return loadFromXLSFile(workbook, inputSize);
            }

        } catch (InvalidFormatException |IOException e) {
            throw new RuntimeException("Unable to open file with Training dataset");
        }
    }

    public static List<PredictionDataSet> loadFromXLSFile(XSSFWorkbook workbook, int inputSize) {
        List<PredictionDataSet> predictionDataSets = Lists.newArrayList();
        Sheet sheet = workbook.sheetIterator().next();
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            PredictionDataSet predictionDataSet = new PredictionDataSet(StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(row.cellIterator(), 0),
                    false
            ).map(cell -> new BigDecimalWrapper(cell.getNumericCellValue()))
                    .collect(Collectors.toList()));
            Verify.verify(predictionDataSet.getInput().size() == inputSize);
            predictionDataSets.add(predictionDataSet);
        }
        return predictionDataSets;
    }
}
