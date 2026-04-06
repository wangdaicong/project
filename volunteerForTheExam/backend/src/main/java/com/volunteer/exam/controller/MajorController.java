package com.volunteer.exam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.exam.common.Result;
import com.volunteer.exam.entity.Major;
import com.volunteer.exam.service.MajorService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/major")
@CrossOrigin
public class MajorController {
    
    @Resource
    private MajorService majorService;

    @GetMapping("/university/{universityId}")
    public Result<List<Major>> getByUniversity(@PathVariable Long universityId) {
        List<Major> list = majorService.getByUniversityId(universityId);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<Major> getById(@PathVariable Long id) {
        Major major = majorService.getById(id);
        return Result.success(major);
    }

    @GetMapping("/category")
    public Result<List<Major>> getByCategory(@RequestParam String category) {
        List<Major> list = majorService.queryByCategory(category);
        return Result.success(list);
    }

    @GetMapping("/search")
    public Result<List<Major>> search(@RequestParam String keyword) {
        List<Major> list = majorService.searchMajors(keyword);
        return Result.success(list);
    }
    
    /**
     * 专业列表（分页）
     */
    @GetMapping("/list")
    public Result list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subCategory,
            @RequestParam(required = false) String degreeLevel,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        try {
            Page<Major> result = majorService.getMajorList(keyword, category, subCategory, degreeLevel, page, size);
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", result.getRecords());
            data.put("total", result.getTotal());
            data.put("page", result.getCurrent());
            data.put("size", result.getSize());
            data.put("totalPages", result.getPages());
            
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 专业详情
     */
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Long id) {
        try {
            Major major = majorService.getById(id);
            if (major == null) {
                return Result.error("专业不存在");
            }
            return Result.success(major);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有学科门类
     */
    @GetMapping("/categories")
    public Result getCategories() {
        try {
            List<String> categories = majorService.getCategories();
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取专业类别
     */
    @GetMapping("/sub-categories")
    public Result getSubCategories(@RequestParam(required = false) String category) {
        try {
            List<String> subCategories = majorService.getSubCategories(category);
            return Result.success(subCategories);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取开设该专业的院校
     */
    @GetMapping("/{id}/universities")
    public Result getUniversities(
            @PathVariable Long id,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String level
    ) {
        try {
            Map<String, Object> result = majorService.getUniversitiesByMajor(id, province, level);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取专业的避坑指南（张雪峰式）
     */
    @GetMapping("/guide/{id}")
    public Result getMajorGuide(@PathVariable Long id) {
        try {
            Major major = majorService.getById(id);
            if (major == null) {
                return Result.error("专业不存在");
            }
            
            Map<String, Object> guide = new HashMap<>();
            guide.put("basic", major);
            
            // 解析标签
            if (major.getZhangxuefengTags() != null && !major.getZhangxuefengTags().isEmpty()) {
                guide.put("tags", major.getZhangxuefengTags().split(","));
            }
            
            // 就业信息
            Map<String, String> employment = new HashMap<>();
            employment.put("reality", major.getEmploymentReality());
            employment.put("salaryLevel", major.getSalaryLevel());
            guide.put("employment", employment);
            
            // 其他信息
            guide.put("civilService", major.getCivilServiceAdvantage());
            guide.put("misconceptions", major.getCommonMisconceptions());
            guide.put("postgraduateNecessity", major.getPostgraduateNecessity());
            
            return Result.success(guide);
        } catch (Exception e) {
            return Result.error("获取避坑指南失败: " + e.getMessage());
        }
    }
}
