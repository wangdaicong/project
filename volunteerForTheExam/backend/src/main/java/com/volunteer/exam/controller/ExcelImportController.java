package com.volunteer.exam.controller;

import com.volunteer.exam.util.ExcelUniversityImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Excel数据导入控制器
 */
@RestController
@RequestMapping("/api/excel-import")
public class ExcelImportController {

    @Autowired
    private ExcelUniversityImporter excelImporter;

    /**
     * 导入Excel高校数据
     * 使用固定路径：项目根目录下的"全国普通高等学校名单.xls"
     */
    @PostMapping("/universities")
    public Map<String, Object> importUniversities() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 使用项目根目录下的Excel文件
            String filePath = "E:/AiProject/project/volunteerForTheExam/全国普通高等学校名单.xls";
            
            int count = excelImporter.importFromExcel(filePath);
            
            result.put("success", true);
            result.put("message", "成功导入 " + count + " 所高校");
            result.put("count", count);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
}
