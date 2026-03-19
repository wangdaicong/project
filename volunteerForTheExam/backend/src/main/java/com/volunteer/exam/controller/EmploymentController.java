package com.volunteer.exam.controller;

import com.volunteer.exam.entity.Industry;
import com.volunteer.exam.entity.CityEmployment;
import com.volunteer.exam.service.EmploymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/employment")
public class EmploymentController {
    
    @Autowired
    private EmploymentService employmentService;
    
    @GetMapping("/major/{majorId}")
    public Map<String, Object> getMajorEmployment(
            @PathVariable Long majorId,
            @RequestParam(required = false) Integer year) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = employmentService.getMajorEmploymentData(majorId, year);
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", data);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/careers/by-major/{majorId}")
    public Map<String, Object> getCareersByMajor(@PathVariable Long majorId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> careers = employmentService.getCareersByMajor(majorId);
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", careers);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/industries")
    public Map<String, Object> getAllIndustries() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Industry> industries = employmentService.getAllIndustries();
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", industries);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/industry/{industryId}")
    public Map<String, Object> getIndustryDetail(@PathVariable Long industryId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = employmentService.getIndustryDetail(industryId);
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", data);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
        }
        return response;
    }
    
    @GetMapping("/cities")
    public Map<String, Object> getCityEmployment(
            @RequestParam(required = false) String tier) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<CityEmployment> cities = employmentService.getCityEmploymentData(tier);
            response.put("success", true);
            response.put("message", "查询成功");
            response.put("data", cities);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
        }
        return response;
    }
    
    @PostMapping("/compare")
    public Map<String, Object> compareMajors(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Long> majorIds = (List<Long>) request.get("majorIds");
            Map<String, Object> data = employmentService.compareMajors(majorIds);
            response.put("success", true);
            response.put("message", "对比成功");
            response.put("data", data);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "对比失败: " + e.getMessage());
        }
        return response;
    }
}
