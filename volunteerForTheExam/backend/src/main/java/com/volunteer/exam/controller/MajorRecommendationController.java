package com.volunteer.exam.controller;

import com.volunteer.exam.service.MajorRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/recommendation")
public class MajorRecommendationController {
    
    @Autowired
    private MajorRecommendationService majorRecommendationService;
    
    @PostMapping("/by-employment")
    public Map<String, Object> recommendByEmployment(@RequestBody Map<String, Object> criteria) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> recommendations = 
                majorRecommendationService.recommendByEmployment(criteria);
            response.put("success", true);
            response.put("message", "推荐成功");
            response.put("data", recommendations);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "推荐失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/by-career")
    public Map<String, Object> recommendByCareer(@RequestParam String careerName) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> recommendations = 
                majorRecommendationService.recommendByCareer(careerName);
            response.put("success", true);
            response.put("message", "推荐成功");
            response.put("data", recommendations);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "推荐失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/major-score/{majorId}")
    public Map<String, Object> getMajorScore(
            @PathVariable Long majorId,
            @RequestParam(required = false) Integer year) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = majorRecommendationService.getMajorScore(majorId, year);
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", data);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
        }
        return response;
    }
}
