package com.yibiai.thesis.controller;

import com.yibiai.thesis.dto.ApiResponse;
import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @PostMapping("/upload")
    public ApiResponse<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String content = extractTextFromFile(file);
            return ApiResponse.success("文件上传成功", content);
        } catch (Exception e) {
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }

    private void appendTableAsMarkdown(StringBuilder sb, XWPFTable table) {
        if (table == null || table.getRows() == null || table.getRows().isEmpty()) {
            return;
        }
        int colCount = 0;
        for (XWPFTableRow row : table.getRows()) {
            if (row == null) continue;
            int c = row.getTableCells() == null ? 0 : row.getTableCells().size();
            colCount = Math.max(colCount, c);
        }
        if (colCount == 0) {
            return;
        }

        boolean separatorAdded = false;
        for (XWPFTableRow row : table.getRows()) {
            if (row == null) continue;
            sb.append("|");
            for (int i = 0; i < colCount; i++) {
                String cellText = "";
                if (row.getTableCells() != null && i < row.getTableCells().size()) {
                    XWPFTableCell cell = row.getCell(i);
                    if (cell != null && cell.getText() != null) {
                        cellText = cell.getText().replace("\r", " ").replace("\n", " ").trim();
                    }
                }
                sb.append(cellText).append("|");
            }
            sb.append("\n");

            if (!separatorAdded) {
                sb.append("|");
                for (int i = 0; i < colCount; i++) {
                    sb.append("---|");
                }
                sb.append("\n");
                separatorAdded = true;
            }
        }
    }

    private String extractTextFromFile(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            filename = "";
        }
        String lowerName = filename.toLowerCase();

        if (lowerName.endsWith(".docx")) {
            try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
                StringBuilder sb = new StringBuilder();
                for (IBodyElement element : doc.getBodyElements()) {
                    if (element.getElementType() == BodyElementType.PARAGRAPH) {
                        XWPFParagraph para = (XWPFParagraph) element;
                        String text = para.getText();
                        if (text != null && !text.isBlank()) {
                            sb.append(text.trim()).append("\n");
                        }
                    } else if (element.getElementType() == BodyElementType.TABLE) {
                        XWPFTable table = (XWPFTable) element;
                        appendTableAsMarkdown(sb, table);
                        sb.append("\n");
                    }
                }
                return sb.toString().trim();
            }
        } else if (lowerName.endsWith(".doc")) {
            try (HWPFDocument doc = new HWPFDocument(file.getInputStream());
                 WordExtractor extractor = new WordExtractor(doc)) {
                return extractor.getText().trim();
            }
        } else {
            return new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
        }
    }

    @PostMapping("/upload/multiple")
    public ApiResponse<String> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        try {
            StringBuilder allContent = new StringBuilder();
            
            for (MultipartFile file : files) {
                String content = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));
                allContent.append("【文件: ").append(file.getOriginalFilename()).append("】\n");
                allContent.append(content).append("\n\n");
            }
            
            return ApiResponse.success("文件上传成功", allContent.toString());
        } catch (Exception e) {
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }
}
