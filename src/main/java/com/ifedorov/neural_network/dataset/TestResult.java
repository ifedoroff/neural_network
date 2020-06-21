package com.ifedorov.neural_network.dataset;

import com.ifedorov.neural_network.BigDecimalWrapper;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class TestResult {
    public final BigDecimalWrapper accuracy;
    public List<? extends TrainingDataSet> dataSets;

    public TestResult(BigDecimalWrapper accuracy, List<? extends TrainingDataSet> trainingTrainingDataSets) {
        this.accuracy = accuracy;
        this.dataSets = trainingTrainingDataSets;
    }

    public void saveTo(File file) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()){
            XSSFSheet sheet = workbook.createSheet();
            for (int i = 0; i < dataSets.size(); i++) {
                XSSFRow row = sheet.createRow(i);
                TrainingDataSet dataSet = dataSets.get(i);
                for (int j = 0; j < dataSet.getInputValues().size(); j++) {
                    row.createCell(j, CellType.NUMERIC).setCellValue(dataSet.getInputValues().get(j).bigDecimal().doubleValue());
                }
                for (int j = 0; j < dataSet.getOutputValues().size(); j++) {
                    row.createCell(dataSet.getInputValues().size() + j, CellType.NUMERIC).setCellValue(dataSet.getActualOutput().get(j).bigDecimal().doubleValue());
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
