package com.volunteer.exam.controller;

import com.volunteer.exam.entity.EnrollmentPath;
import com.volunteer.exam.service.EnrollmentPlanningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/enrollment")
public class EnrollmentPlanningController {
    
    @Autowired
    private EnrollmentPlanningService enrollmentPlanningService;
    
    @GetMapping("/paths")
    public Map<String, Object> getAllPaths() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<EnrollmentPath> paths = enrollmentPlanningService.getAllPaths();
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", paths);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/path/{pathId}")
    public Map<String, Object> getPathDetail(@PathVariable Long pathId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = enrollmentPlanningService.getPathDetail(pathId);
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", data);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/recommend")
    public Map<String, Object> recommendPaths(@RequestBody Map<String, Object> studentProfile) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> recommendations = 
                enrollmentPlanningService.recommendPaths(studentProfile);
            response.put("success", true);
            response.put("message", "推荐成功");
            response.put("data", recommendations);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "推荐失败: " + e.getMessage());
        }
        return response;
    }
}
