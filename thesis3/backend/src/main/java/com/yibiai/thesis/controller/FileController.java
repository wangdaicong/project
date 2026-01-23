package com.yibiai.thesis.controller;

import com.yibiai.thesis.dto.ApiResponse;
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
            String content;
            String filename = file.getOriginalFilename();
            
            if (filename != null && (filename.endsWith(".txt") || filename.endsWith(".md"))) {
                content = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));
            } else {
                content = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));
            }
            
            return ApiResponse.success("文件上传成功", content);
        } catch (Exception e) {
            return ApiResponse.error("文件上传失败: " + e.getMessage());
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
