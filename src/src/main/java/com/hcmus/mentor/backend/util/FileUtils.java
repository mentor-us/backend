package com.hcmus.mentor.backend.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class FileUtils {

    public static File generateExcel(
            List<String> headers,
            List<List<String>> data,
            List<Integer> remainColumnIndexes,
            String fileName) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");

        CellStyle headerCellStyle = generateHeaderStyle(workbook);

        Row headerRow = sheet.createRow(0);
        int columnIndex = 0;
        for (int i = 0; i < headers.size(); i++) {
            if (remainColumnIndexes.size() == 1 || remainColumnIndexes.contains(i)) {
                Cell cell = headerRow.createCell(columnIndex);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerCellStyle);
                columnIndex++;
            }
        }

        int rowNum = 1;
        for (List<String> rowData : data) {
            Row row = sheet.createRow(rowNum++);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);

            if (rowNum % 2 == 0) {
                cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
            } else {
                cellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            }

            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            columnIndex = 0;
            for (int i = 0; i < rowData.size(); i++) {
                if (remainColumnIndexes.size() == 1 || remainColumnIndexes.contains(i)) {
                    Cell cell = row.createCell(columnIndex);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue(rowData.get(i));
                    columnIndex++;
                }
            }
        }
        autoSizeColumns(workbook);

        File outputFile = new File(fileName);
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputFile;
    }

    public static void autoSizeColumns(Workbook workbook) {
        int numberOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (sheet.getPhysicalNumberOfRows() > 0) {
                Row row = sheet.getRow(sheet.getFirstRowNum());
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    int columnIndex = cell.getColumnIndex();
                    sheet.autoSizeColumn(columnIndex);
                }
            }
        }
    }

    public static void formatWorkbook(Workbook workbook) {

        int numberOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);

            // Create cell styles for even and odd rows
            CellStyle evenRowStyle = workbook.createCellStyle();
            evenRowStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
            evenRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle oddRowStyle = workbook.createCellStyle();
            oddRowStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
            oddRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            CellStyle headerCellStyle = generateHeaderStyle(workbook);
            for (Row row : sheet) {
                int rowNum = row.getRowNum();
                CellStyle rowStyle = rowNum % 2 == 0 ? evenRowStyle : oddRowStyle;
                for (Cell cell : row) {
                    if (rowNum == 0) {
                        cell.setCellStyle(headerCellStyle);
                    } else {
                        rowStyle.setBorderTop(BorderStyle.THIN);
                        rowStyle.setBorderBottom(BorderStyle.THIN);
                        rowStyle.setBorderLeft(BorderStyle.THIN);
                        rowStyle.setBorderRight(BorderStyle.THIN);
                        cell.setCellStyle(rowStyle);
                    }
                }
            }
        }
    }

    private static CellStyle generateHeaderStyle(Workbook workbook) {
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setBold(true);
        headerCellStyle.setFont(headerFont);
        return headerCellStyle;
    }
}
