package com.volunteer.exam.controller;

import com.volunteer.exam.entity.SubjectRequirement;
import com.volunteer.exam.service.SubjectRequirementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 选科要求查询控制器
 */
@RestController
@RequestMapping("/api/subject-requirement")
@CrossOrigin(origins = "*")
public class SubjectRequirementController {

    @Autowired
    private SubjectRequirementService subjectRequirementService;

    /**
     * 按选科查询专业和院校
     * @param subjects 选科组合，如：物理,化学,生物
     * @param province 省份
     * @param year 年份
     * @param degreeLevel 学历层次：本科/专科
     * @param universityType 院校性质
     * @return 可报考的专业和院校列表
     */
    @GetMapping("/query-by-subjects")
    public Map<String, Object> queryBySubjects(
            @RequestParam String subjects,
            @RequestParam(required = false, defaultValue = "广东") String province,
            @RequestParam(required = false, defaultValue = "2024") Integer year,
            @RequestParam(required = false) String degreeLevel,
            @RequestParam(required = false) String universityType) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            List<SubjectRequirement> allData = subjectRequirementService.queryBySubjects(
                subjects, province, year, degreeLevel, universityType);
            
            // 分类：可报考和不可报考
            List<SubjectRequirement> canApply = new ArrayList<>();
            List<SubjectRequirement> cannotApply = new ArrayList<>();
            
            for (SubjectRequirement item : allData) {
                if (item.getCanApply() != null && item.getCanApply() == 1) {
                    canApply.add(item);
                } else {
                    cannotApply.add(item);
                }
            }
            
            // 统计专业门类
            Map<String, Long> categoryCount = canApply.stream()
                .collect(Collectors.groupingBy(
                    SubjectRequirement::getMajorCategory,
                    Collectors.counting()
                ));
            
            result.put("success", true);
            result.put("canApply", canApply);
            result.put("cannotApply", cannotApply);
            result.put("canApplyCount", canApply.size());
            result.put("cannotApplyCount", cannotApply.size());
            result.put("categoryCount", categoryCount);
            result.put("total", allData.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 按专业查询选科要求
     * @param majorName 专业名称
     * @param province 省份
     * @param year 年份
     * @return 该专业的选科要求列表
     */
    @GetMapping("/query-by-major")
    public Map<String, Object> queryByMajor(
            @RequestParam String majorName,
            @RequestParam(required = false, defaultValue = "广东") String province,
            @RequestParam(required = false, defaultValue = "2024") Integer year) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            List<SubjectRequirement> data = subjectRequirementService.queryByMajor(
                majorName, province, year);
            
            result.put("success", true);
            result.put("data", data);
            result.put("total", data.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 按高校查询选科要求
     * @param universityName 院校名称
     * @param province 省份
     * @param year 年份
     * @return 该院校各专业的选科要求列表
     */
    @GetMapping("/query-by-university")
    public Map<String, Object> queryByUniversity(
            @RequestParam String universityName,
            @RequestParam(required = false, defaultValue = "广东") String province,
            @RequestParam(required = false, defaultValue = "2024") Integer year) {
        
        Map<String, Object> result = new HashMap<>();
        try {
            List<SubjectRequirement> data = subjectRequirementService.queryByUniversity(
                universityName, province, year);
            
            result.put("success", true);
            result.put("data", data);
            result.put("total", data.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 获取所有专业门类
     */
    @GetMapping("/major-categories")
    public Map<String, Object> getMajorCategories() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<String> categories = subjectRequirementService.getMajorCategories();
            result.put("success", true);
            result.put("data", categories);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 获取指定门类下的专业列表
     */
    @GetMapping("/majors-by-category")
    public Map<String, Object> getMajorsByCategory(@RequestParam String category) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<String> majors = subjectRequirementService.getMajorsByCategory(category);
            result.put("success", true);
            result.put("data", majors);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
