package com.example.bicolorsphere.service;

import com.example.bicolorsphere.domain.SsqDraw;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SsqExcelExportService {

    public byte[] exportDraws(List<SsqDraw> draws) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("ssq_draws");

            int r = 0;
            Row header = sheet.createRow(r++);
            header.createCell(0).setCellValue("drawNo");
            header.createCell(1).setCellValue("drawDate");
            header.createCell(2).setCellValue("red1");
            header.createCell(3).setCellValue("red2");
            header.createCell(4).setCellValue("red3");
            header.createCell(5).setCellValue("red4");
            header.createCell(6).setCellValue("red5");
            header.createCell(7).setCellValue("red6");
            header.createCell(8).setCellValue("blue");

            DateTimeFormatter df = DateTimeFormatter.ISO_LOCAL_DATE;
            for (SsqDraw d : draws) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(d.getDrawNo());
                row.createCell(1).setCellValue(d.getDrawDate() == null ? "" : df.format(d.getDrawDate()));
                for (int i = 0; i < 6; i++) {
                    row.createCell(2 + i).setCellValue(d.getReds().get(i));
                }
                row.createCell(8).setCellValue(d.getBlue());
            }

            for (int c = 0; c <= 8; c++) {
                sheet.autoSizeColumn(c);
            }

            wb.write(bos);
            return bos.toByteArray();
        }
    }
}
