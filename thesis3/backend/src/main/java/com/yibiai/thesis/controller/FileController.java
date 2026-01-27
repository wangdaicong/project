package com.yibiai.thesis.controller;

import com.yibiai.thesis.dto.ApiResponse;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
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

    private String extractTextFromFile(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            filename = "";
        }
        String lowerName = filename.toLowerCase();

        if (lowerName.endsWith(".docx")) {
            try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
                StringBuilder sb = new StringBuilder();
                for (XWPFParagraph para : doc.getParagraphs()) {
                    sb.append(para.getText()).append("\n");
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
