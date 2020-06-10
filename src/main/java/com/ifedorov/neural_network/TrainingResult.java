package com.ifedorov.neural_network;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class TrainingResult {
    public final long epochs;
    public final BigDecimalWrapper accuracy;
    public List<? extends TrainingDataSet> trainingDataSets;

    public TrainingResult(long epochs, BigDecimalWrapper accuracy, List<? extends TrainingDataSet> trainingDataSets) {
        this.epochs = epochs;
        this.accuracy = accuracy;
        this.trainingDataSets = trainingDataSets;
    }
}
