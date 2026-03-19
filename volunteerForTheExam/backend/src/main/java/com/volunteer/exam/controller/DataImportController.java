package com.volunteer.exam.controller;

import com.volunteer.exam.common.Result;
import com.volunteer.exam.service.DataImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 数据导入控制器
 * 提供Excel批量导入功能
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/import")
@CrossOrigin
public class DataImportController {

    @Autowired
    private DataImportService dataImportService;

    /**
     * 导入院校数据
     * POST /api/admin/import/universities
     */
    @PostMapping("/universities")
    public Result importUniversities(@RequestParam("file") MultipartFile file) {
        log.info("开始导入院校数据，文件名: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        
        if (!file.getOriginalFilename().endsWith(".xlsx") && 
            !file.getOriginalFilename().endsWith(".xls")) {
            return Result.error("只支持Excel文件（.xlsx或.xls）");
        }
        
        try {
            DataImportService.ImportResult result = dataImportService.importUniversities(file);
            
            if (result.isSuccess()) {
                String message = String.format("导入完成！总计: %d, 成功: %d, 失败: %d", 
                    result.getTotal(), result.getSuccessCount(), result.getFailCount());
                return Result.success(message, result);
            } else {
                return Result.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("导入院校数据失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 导入专业数据
     * POST /api/admin/import/majors
     */
    @PostMapping("/majors")
    public Result importMajors(@RequestParam("file") MultipartFile file) {
        log.info("开始导入专业数据，文件名: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        
        if (!file.getOriginalFilename().endsWith(".xlsx") && 
            !file.getOriginalFilename().endsWith(".xls")) {
            return Result.error("只支持Excel文件（.xlsx或.xls）");
        }
        
        try {
            DataImportService.ImportResult result = dataImportService.importMajors(file);
            
            if (result.isSuccess()) {
                String message = String.format("导入完成！总计: %d, 成功: %d, 失败: %d", 
                    result.getTotal(), result.getSuccessCount(), result.getFailCount());
                return Result.success(message, result);
            } else {
                return Result.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("导入专业数据失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 导入历年分数线数据
     * POST /api/admin/import/scores
     */
    @PostMapping("/scores")
    public Result importScores(@RequestParam("file") MultipartFile file) {
        log.info("开始导入分数线数据，文件名: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        
        if (!file.getOriginalFilename().endsWith(".xlsx") && 
            !file.getOriginalFilename().endsWith(".xls")) {
            return Result.error("只支持Excel文件（.xlsx或.xls）");
        }
        
        try {
            DataImportService.ImportResult result = dataImportService.importAdmissionScores(file);
            
            if (result.isSuccess()) {
                String message = String.format("导入完成！总计: %d, 成功: %d, 失败: %d", 
                    result.getTotal(), result.getSuccessCount(), result.getFailCount());
                return Result.success(message, result);
            } else {
                return Result.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("导入分数线数据失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 下载导入模板
     * GET /api/admin/import/template/{type}
     */
    @GetMapping("/template/{type}")
    public Result downloadTemplate(@PathVariable String type) {
        // 返回模板文件下载链接或说明
        String templateInfo = "";
        
        switch (type) {
            case "universities":
                templateInfo = "院校数据模板：院校名称 | 省份 | 城市 | 层次 | 类型 | 简介 | 特色 | 官网 | 电话 | 地址 | 排名";
                break;
            case "majors":
                templateInfo = "专业数据模板：专业名称 | 所属院校 | 类别 | 学制 | 学位 | 简介 | 主要课程 | 就业方向";
                break;
            case "scores":
                templateInfo = "分数线模板：院校名称 | 专业名称 | 省份 | 年份 | 批次 | 科目类型 | 最低分 | 平均分 | 最高分 | 最低位次 | 招生人数";
                break;
            default:
                return Result.error("未知的模板类型");
        }
        
        return Result.success(templateInfo);
    }
}
