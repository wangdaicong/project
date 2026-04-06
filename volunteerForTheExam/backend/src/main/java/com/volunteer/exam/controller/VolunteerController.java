package com.volunteer.exam.controller;

import com.volunteer.exam.common.Result;
import com.volunteer.exam.entity.VolunteerApplication;
import com.volunteer.exam.entity.VolunteerDetail;
import com.volunteer.exam.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 志愿填报控制器
 */
@RestController
@RequestMapping("/api/volunteer")
public class VolunteerController {
    
    @Autowired
    private VolunteerService volunteerService;
    
    /**
     * 创建志愿填报记录
     */
    @PostMapping("/create")
    public Result<Long> createApplication(@RequestBody VolunteerApplication application) {
        try {
            Long applicationId = volunteerService.createApplication(application);
            return Result.success("创建成功", applicationId);
        } catch (Exception e) {
            return Result.error("创建失败：" + e.getMessage());
        }
    }
    
    /**
     * 保存志愿详情
     */
    @PostMapping("/save-details")
    public Result<String> saveVolunteerDetails(
            @RequestParam Long applicationId,
            @RequestBody List<VolunteerDetail> details) {
        try {
            volunteerService.saveVolunteerDetails(applicationId, details);
            return Result.success("保存成功", null);
        } catch (Exception e) {
            return Result.error("保存失败：" + e.getMessage());
        }
    }
    
    /**
     * 提交并分析志愿填报方案
     */
    @PostMapping("/analyze/{applicationId}")
    public Result<Map<String, Object>> analyzeVolunteers(@PathVariable Long applicationId) {
        try {
            Map<String, Object> result = volunteerService.analyzeVolunteers(applicationId);
            if (result.isEmpty()) {
                return Result.error("志愿填报记录不存在或无志愿详情");
            }
            return Result.success("分析成功", result);
        } catch (Exception e) {
            return Result.error("分析失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取志愿填报详情
     */
    @GetMapping("/detail/{applicationId}")
    public Result<Map<String, Object>> getApplicationDetail(@PathVariable Long applicationId) {
        try {
            Map<String, Object> result = volunteerService.getApplicationDetail(applicationId);
            if (result.isEmpty()) {
                return Result.error("志愿填报记录不存在");
            }
            return Result.success("查询成功", result);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取推荐院校列表
     */
    @GetMapping("/recommend")
    public Result<List<Map<String, Object>>> getRecommendedUniversities(
            @RequestParam String province,
            @RequestParam Integer score,
            @RequestParam(defaultValue = "理科") String category) {
        try {
            List<Map<String, Object>> recommendations = volunteerService.getRecommendedUniversities(province, score, category);
            return Result.success("查询成功", recommendations);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户的志愿填报记录列表
     */
    @GetMapping("/list")
    public Result<List<VolunteerApplication>> getUserApplications(@RequestParam Long userId) {
        try {
            List<VolunteerApplication> applications = volunteerService.getUserApplications(userId);
            return Result.success("查询成功", applications);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
