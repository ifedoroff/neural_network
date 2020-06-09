package com.ifedorov.neural_network;

import com.google.common.collect.Lists;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NormalizedTrainingDataSet extends TrainingDataSet {

    public NormalizedTrainingDataSet(TrainingDataSet trainingDataSet) {
        this(trainingDataSet.input, trainingDataSet.output);
    }
    public NormalizedTrainingDataSet(List<BigDecimalWrapper> input, List<BigDecimalWrapper> output) {
        super(input, output);
        ensureNormalized(input);
        ensureNormalized(output);
    }

    private void ensureNormalized(List<BigDecimalWrapper> values) {
        Optional<BigDecimalWrapper> denormalized = values.stream().filter(value -> value.compareTo(BigDecimalWrapper.ZERO) < 0 || value.compareTo(BigDecimalWrapper.ONE) > 0)
                .findFirst();
        if(denormalized.isPresent()) {
            throw new IllegalArgumentException("One of the values is not normalized: " + values);
        }
    }

    public static void saveTo(List<NormalizedTrainingDataSet> dataSets, File file) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()){
            XSSFSheet sheet = workbook.createSheet();
            for (int i = 0; i < dataSets.size(); i++) {
                XSSFRow row = sheet.createRow(i);
                NormalizedTrainingDataSet dataSet = dataSets.get(i);
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

    public static List<NormalizedTrainingDataSet> normalize(List<TrainingDataSet> trainingDataSets) {
        List<BigDecimalWrapper> maxValues = Lists.newArrayList(trainingDataSets.get(0).input);
        List<BigDecimalWrapper> minValues = Lists.newArrayList(trainingDataSets.get(0).input);
        for (TrainingDataSet dataSet : trainingDataSets) {
            List<BigDecimalWrapper> inputs = dataSet.input;
            for (int i = 0; i < inputs.size(); i++) {
                BigDecimalWrapper maxValue = maxValues.get(i);
                BigDecimalWrapper minValue = minValues.get(i);
                BigDecimalWrapper currentValue = inputs.get(i);
                if(maxValue.compareTo(currentValue) < 0) {
                    maxValues.set(i, currentValue);
                }
                if(minValue.compareTo(currentValue) > 0) {
                    minValues.set(i, currentValue);
                }
            }
        }
        return trainingDataSets.stream()
                .map(trainingDataSet -> {
                    List<BigDecimalWrapper> inputs = trainingDataSet.input;
                    for (int i = 0; i < inputs.size(); i++) {
                        BigDecimalWrapper currentValue = inputs.get(i);
                        BigDecimalWrapper minValue = minValues.get(i);
                        BigDecimalWrapper maxValue = maxValues.get(i);
                        if(currentValue.equals(minValue) && maxValue.equals(minValue)) {
                            inputs.set(i, currentValue);
                        } else {
                            inputs.set(i, currentValue.subtract(minValue).divide(maxValue.subtract(minValue)));
                        }
                    }
                    return new NormalizedTrainingDataSet(trainingDataSet);
                }).collect(Collectors.toList());
    }
}
