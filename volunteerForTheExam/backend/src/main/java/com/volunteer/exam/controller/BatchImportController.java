package com.volunteer.exam.controller;

import com.volunteer.exam.service.BatchDataImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 批量数据导入控制器
 */
@RestController
@RequestMapping("/api/batch-import")
public class BatchImportController {

    @Autowired
    private BatchDataImportService batchImportService;

    /**
     * 上传并导入高校CSV文件
     */
    @PostMapping("/universities/csv")
    public Map<String, Object> importUniversitiesCSV(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 保存上传文件
            String filePath = saveUploadFile(file);
            
            // 导入数据
            int count = batchImportService.importUniversitiesFromCSV(filePath);
            
            response.put("code", 200);
            response.put("message", "导入成功");
            response.put("data", Map.of("count", count));
            
            // 删除临时文件
            new File(filePath).delete();
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "导入失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 上传并导入专业JSON文件
     */
    @PostMapping("/majors/json")
    public Map<String, Object> importMajorsJSON(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String filePath = saveUploadFile(file);
            int count = batchImportService.importMajorsFromJSON(filePath);
            
            response.put("code", 200);
            response.put("message", "导入成功");
            response.put("data", Map.of("count", count));
            
            new File(filePath).delete();
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "导入失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 获取数据统计
     */
    @GetMapping("/statistics")
    public Map<String, Object> getStatistics() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> stats = batchImportService.getDataStatistics();
            
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", stats);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取统计失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 数据验证
     */
    @GetMapping("/validate")
    public Map<String, Object> validateData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> validation = batchImportService.validateData();
            
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", validation);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "验证失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 保存上传文件
     */
    private String saveUploadFile(MultipartFile file) throws Exception {
        String uploadDir = System.getProperty("java.io.tmpdir");
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String filePath = uploadDir + File.separator + fileName;
        
        file.transferTo(new File(filePath));
        return filePath;
    }
}
