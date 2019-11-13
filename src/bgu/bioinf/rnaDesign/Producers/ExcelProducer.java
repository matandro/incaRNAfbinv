package bgu.bioinf.rnaDesign.Producers;

import bgu.bioinf.rnaDesign.Runners.SingleResultCallable;
import bgu.bioinf.rnaDesign.db.JobResultEntity;
import bgu.bioinf.rnaDesign.model.JobInfoModel;
import bgu.bioinf.rnaDesign.model.JobResultModel;
import bgu.bioinf.rnaDesign.model.SingleResultModel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * Created by matan on 15/12/14.
 */
public class ExcelProducer {
    private Workbook excelWorkbook;
    private String jobId;
    private ResultProducer jobRetriever;

    public ExcelProducer(String jobId) {
        this.jobId = jobId;
    }

    public boolean init() {
        boolean result = false;
        try {
            jobRetriever = new ResultProducer(jobId);
            result = jobRetriever.isResultsReady();
            if (result)
                result = jobRetriever.initiateResults();
        } catch (Exception ignore) {
        }
        return result;
    }

    private String getShortName() {
        String shortName = jobRetriever.getJobInformation().getQueryName();
        if (shortName == null || "".equals(shortName)) {
            shortName = jobRetriever.getJobInformation().getJobId();
        } else {
            shortName = jobRetriever.getJobInformation().getJobId() + "_" + shortName.trim().replace(' ', '_');
        }
        if (shortName.length() > 15) {
            shortName = shortName.substring(0, 15);
        }
        return shortName;
    }

    public String getOutputFileName() {
        return getShortName() + ".xlsx";
    }

    public String writeData() {
        String excelPath = null;
        FileOutputStream fileOutputStream = null;
        try {
            excelWorkbook = new SXSSFWorkbook(100);
            // Generate bold font
            Font boldFont= excelWorkbook.createFont();
            boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
            CellStyle boldCellStyle= excelWorkbook.createCellStyle();
            boldCellStyle.setFont(boldFont);
            createGeneralInfoSheet(boldCellStyle);
            Sheet targetSheet = excelWorkbook.createSheet("Results");
            // Initiate writing for target sheet
            int rowNo = 0;
            Row row = targetSheet.createRow(rowNo++);
            row.setRowStyle(boldCellStyle);
            int cellNo = 0;
            row.createCell(cellNo++).setCellValue("Result No");
            row.createCell(cellNo++).setCellValue("Seed Sequences");
            row.createCell(cellNo++).setCellValue("Result Sequence");
            row.createCell(cellNo++).setCellValue("Structure");
            if (this.jobRetriever.getJobInformation().getVersion() == 2) {
                row.createCell(cellNo++).setCellValue("Design score");
            }
            row.createCell(cellNo++).setCellValue("BP distance");
            row.createCell(cellNo++).setCellValue("Shapiro structure");
            row.createCell(cellNo++).setCellValue("Shapiro coarse structure");
            row.createCell(cellNo++).setCellValue("Shapiro distance");
            row.createCell(cellNo++).setCellValue("Energy Score");
            /*
            if (this.jobRetriever.getJobInformation().getVersion() == 2) {
                row.createCell(cellNo++).setCellValue("Aligned tree");
                row.createCell(cellNo++).setCellValue("Target tree");
            }*/

            JobResultModel jobResultModel = jobRetriever.getJobResults();

            for (SingleResultModel result : jobResultModel.getResults()) {
                row = targetSheet.createRow(rowNo++);
                cellNo = 0;
                row.createCell(cellNo++).setCellValue(result.getResultNo());
                row.createCell(cellNo++).setCellValue(result.getSeedSequence());
                row.createCell(cellNo++).setCellValue(result.getResultSequence());
                row.createCell(cellNo++).setCellValue(result.getResultStructure());
                if (this.jobRetriever.getJobInformation().getVersion() == 2) {
                    row.createCell(cellNo++).setCellValue(result.getDesignScore());
                }
                row.createCell(cellNo++).setCellValue(result.getBpDistance());
                row.createCell(cellNo++).setCellValue(result.getResultShapiro());
                row.createCell(cellNo++).setCellValue(result.getResultShapiroCoarse());
                row.createCell(cellNo++).setCellValue(result.getShapiroDistance());
                row.createCell(cellNo++).setCellValue(result.getEnergyScore());
                /*
                if (this.jobRetriever.getJobInformation().getVersion() == 2) {
                    row.createCell(cellNo++).setCellValue();
                    row.createCell(cellNo++).setCellValue();
                }*/
            }
            File xls = File.createTempFile(getShortName(), ".xlsx");
            excelPath = xls.getAbsolutePath();
            fileOutputStream = new FileOutputStream(xls);
            excelWorkbook.write(fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            excelPath = null;
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception ignore) {
                }
            }
        }
        return excelPath;
    }

    private void createGeneralInfoSheet(CellStyle boldCellStyle) {
        JobInfoModel jobInformation = jobRetriever.getJobInformation();
        int cellNo = 0;
        int rowNo = 0;
        CreationHelper createHelper = excelWorkbook.getCreationHelper();
        CellStyle dataCellStyle = excelWorkbook.createCellStyle();
        dataCellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("MM/dd/yyyy HH:mm:ss"));
        Cell cell;
        Sheet generalInfo = excelWorkbook.createSheet("Query");
        // Write general job info
        Row row = generalInfo.createRow(rowNo++);
        row.setRowStyle(boldCellStyle);
        row.createCell(cellNo++).setCellValue("Job ID");
        row.createCell(cellNo++).setCellValue("Job Name");
        row.createCell(cellNo++).setCellValue("Target Sequence");
        row.createCell(cellNo++).setCellValue("Structure constraints");
        row.createCell(cellNo++).setCellValue("Target Energy");
        row.createCell(cellNo++).setCellValue("Target Mutational Robustness");
        row.createCell(cellNo++).setCellValue("Motif constraint");
        row.createCell(cellNo++).setCellValue("Submission time");
        row.createCell(cellNo++).setCellValue("Ready time");
        row = generalInfo.createRow(rowNo++);
        cellNo = 0;
        row.createCell(cellNo++).setCellValue(jobId);
        row.createCell(cellNo++).setCellValue(jobInformation.getQueryName());
        row.createCell(cellNo++).setCellValue(jobInformation.getQuerySequence());
        row.createCell(cellNo++).setCellValue(jobInformation.getQueryStructure());
        row.createCell(cellNo++).setCellValue((jobInformation.getTargetEnergy() == -1000) ? ""
                : jobInformation.getTargetEnergy() + "");
        row.createCell(cellNo++).setCellValue((jobInformation.getTargetMR() == -1000) ? ""
                : jobInformation.getTargetMR() + "");
        row.createCell(cellNo++).setCellValue((jobInformation.getMotifConstraint() == null
                || "".equals(jobInformation.getMotifConstraint())) ? "" : jobInformation.getMotifConstraint().split("_")[1]);
        cell = row.createCell(cellNo++);
        cell.setCellStyle(dataCellStyle);
        cell.setCellValue(jobInformation.getStartTime());
        cell = row.createCell(cellNo++);
        cell.setCellStyle(dataCellStyle);
        cell.setCellValue(jobInformation.getEndTime());
        rowNo++;
        cellNo = 0;
        // Write Type and parameters
        row = generalInfo.createRow(rowNo++);
        row.createCell(cellNo++).setCellValue("Seed type");
        row.setRowStyle(boldCellStyle);
        switch (jobInformation.getSeedType()) {
            case INCARNATION:
                row.createCell(cellNo++).setCellValue("GC% content");
                row.createCell(cellNo++).setCellValue("GC% error");
                row = generalInfo.createRow(rowNo++);
                cellNo = 0;
                row.createCell(cellNo++).setCellValue(jobInformation.getSeedTypeString());
                row.createCell(cellNo++).setCellValue(jobInformation.getGcContent());
                row.createCell(cellNo++).setCellValue(jobInformation.getGcError());
                break;
            case INITIAL:
                row.createCell(cellNo++).setCellValue("Seed");
                row = generalInfo.createRow(rowNo++);
                cellNo = 0;
                row.createCell(cellNo++).setCellValue(jobInformation.getSeedTypeString());
                row.createCell(cellNo++).setCellValue(jobInformation.getSeedSequence());
                break;
            default:
                break;
        }
    }
}
