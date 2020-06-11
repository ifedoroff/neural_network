package com.ifedorov.neural_network;

import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TrainingDataSet {

    public final List<BigDecimalWrapper> input;
    public final List<BigDecimalWrapper> expectedOutput;
    private List<BigDecimalWrapper> actualOutput;
    private BigDecimalWrapper accuracy;

    public TrainingDataSet(List<BigDecimalWrapper> input, List<BigDecimalWrapper> expectedOutput) {
        this.input = input;
        this.expectedOutput = expectedOutput;
    }

    public List<BigDecimalWrapper> getActualOutput() {
        return actualOutput;
    }

    public void setActualOutput(List<BigDecimalWrapper> actualOutput) {
        this.actualOutput = actualOutput;
    }

    public BigDecimalWrapper getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(BigDecimalWrapper accuracy) {
        this.accuracy = accuracy;
    }

    public static List<TrainingDataSet> loadFromTextFile(Path path) {
        try {
            return Files.lines(path)
                    .map(TrainingDataSet::fromCSVLine)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Unable to read data sets from " + path);
        }
    }

    private static TrainingDataSet fromCSVLine(String line) {
        return new TrainingDataSet(
                Arrays.stream(line.split("\\|")[0].split(","))
                        .map(Double::valueOf)
                        .map(BigDecimalWrapper::new)
                        .collect(Collectors.toList()),
                Arrays.stream(line.split("\\|")[1].split(","))
                        .map(Double::valueOf)
                        .map(BigDecimalWrapper::new)
                        .collect(Collectors.toList())
        );
    }

    public static List<TrainingDataSet> loadFromXLSFile(InputStream is, int inputSize, int outputSize) {

        try {
            try(XSSFWorkbook workbook = new XSSFWorkbook(is)) {
                return loadFromXLSFile(workbook, inputSize, outputSize);
            }

        } catch (IOException e) {
            throw new RuntimeException("Unable to open file with Training dataset", e);
        }
    }

    public static List<TrainingDataSet> loadFromXLSFile(Path path, int inputSize, int outputSize) {

        try {
            try(XSSFWorkbook workbook = new XSSFWorkbook(path.toFile())) {
                return loadFromXLSFile(workbook, inputSize, outputSize);
            }

        } catch (InvalidFormatException|IOException e) {
            throw new RuntimeException("Unable to open file with Training dataset", e);
        }
    }

    public static List<TrainingDataSet> loadFromXLSFile(XSSFWorkbook workbook, int inputSize, int outputSize) {
        List<TrainingDataSet> trainingDataSets = Lists.newArrayList();
        Sheet sheet = workbook.sheetIterator().next();
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            List<BigDecimalWrapper> rowValues = StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(row.cellIterator(), 0),
                    false
            ).map(cell -> new BigDecimalWrapper(cell.getNumericCellValue()))
                    .collect(Collectors.toList());
            Verify.verify(inputSize + outputSize == rowValues.size(), "Incorrect number of parameters at row: " + row.getRowNum());
            trainingDataSets.add(new TrainingDataSet(rowValues.subList(0, inputSize), rowValues.subList(inputSize, inputSize + outputSize)));
        }
        return trainingDataSets;
    }

    public static void saveTo(List<? extends TrainingDataSet> dataSets, File file) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()){
            XSSFSheet sheet = workbook.createSheet();
            for (int i = 0; i < dataSets.size(); i++) {
                XSSFRow row = sheet.createRow(i);
                TrainingDataSet dataSet = dataSets.get(i);
                for (int j = 0; j < dataSet.input.size(); j++) {
                    row.createCell(j).setCellValue(dataSet.input.get(j).bigDecimal().doubleValue());
                }
            }
            try (OutputStream os = new FileOutputStream(file)) {
                workbook.write(os);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save normalized weights to file", e);
        }
    }
}
