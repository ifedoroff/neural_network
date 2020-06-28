package com.ifedorov.neural_network.train.dataset;

import com.ifedorov.neural_network.train.BigDecimalWrapper;
import com.ifedorov.neural_network.train.Utf8ResourceBundle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.ResourceBundle;

public class TestResult {

    public static final ResourceBundle localization = Utf8ResourceBundle.getBundle("Messages");

    public final BigDecimalWrapper accuracy;
    public List<? extends TrainingDataSet> dataSets;

    public TestResult(BigDecimalWrapper accuracy, List<? extends TrainingDataSet> trainingTrainingDataSets) {
        this.accuracy = accuracy;
        this.dataSets = trainingTrainingDataSets;
    }

    public void saveTo(File file) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()){
            XSSFSheet sheet = workbook.createSheet();
            writeHeader(sheet);
            for (int i = 0; i < dataSets.size(); i++) {
                XSSFRow row = sheet.createRow(i + 1);
                TrainingDataSet dataSet = dataSets.get(i);
                for (int j = 0; j < dataSet.getInputValues().size(); j++) {
                    row.createCell(j, CellType.NUMERIC).setCellValue(dataSet.getInputValues().get(j).bigDecimal().doubleValue());
                }
                for (int j = 0; j < dataSet.getOutputValues().size(); j++) {
                    row.createCell(dataSet.getInputValues().size() + j, CellType.NUMERIC).setCellValue(dataSet.getOutputValues().get(j).bigDecimal().doubleValue());
                }

                for (int j = 0; j < dataSet.getActualOutput().size(); j++) {
                    row.createCell(dataSet.getInputValues().size() + dataSet.getOutputValues().size() + j, CellType.NUMERIC).setCellValue(dataSet.getActualOutput().get(j).bigDecimal().doubleValue());
                }
            }
            try (OutputStream os = new FileOutputStream(file)) {
                workbook.write(os);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save normalized weights to file", e);
        }
    }

    private void writeHeader(XSSFSheet sheet) {
        XSSFRow header = sheet.createRow(0);
        int k = 0;
        for (k = 0; k < dataSets.get(0).getInputValues().size(); k++) {
            header.createCell(k).setCellValue(localization.getString("parameter") + " " + (k + 1));
            sheet.autoSizeColumn(k);
        }
        for (int j = 0; j < dataSets.get(0).getOutputValues().size(); j++, k++) {
            header.createCell(k + j).setCellValue(localization.getString("expectedOutput") + " " + (j + 1));
            sheet.autoSizeColumn(k + j);
        }
        for (int j = 0; j < dataSets.get(0).getActualOutput().size(); j++, k++) {
            header.createCell(k + j).setCellValue(localization.getString("actualOutput") + " " + (j + 1));
            sheet.autoSizeColumn(k + j);
        }
    }
}
