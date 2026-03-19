package com.volunteer.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.volunteer.exam.common.Result;
import com.volunteer.exam.entity.Major;
import com.volunteer.exam.entity.University;
import com.volunteer.exam.mapper.MajorMapper;
import com.volunteer.exam.mapper.UniversityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 爬虫数据导入API
 * 专门为Python爬虫提供的数据导入接口
 */
@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin
public class CrawlerApiController {
    
    @Resource
    private UniversityMapper universityMapper;
    
    @Resource
    private MajorMapper majorMapper;
    
    /**
     * 插入或更新院校数据
     * POST /api/university
     */
    @PostMapping("/university")
    public Result insertUniversity(@RequestBody Map<String, Object> data) {
        try {
            String name = (String) data.get("name");
            if (name == null || name.isEmpty()) {
                return Result.error("院校名称不能为空");
            }
            
            // 检查是否已存在
            QueryWrapper<University> wrapper = new QueryWrapper<>();
            wrapper.eq("name", name);
            University existing = universityMapper.selectOne(wrapper);
            
            University university = existing != null ? existing : new University();
            
            // 设置字段
            university.setName(name);
            if (data.containsKey("province")) university.setProvince((String) data.get("province"));
            if (data.containsKey("city")) university.setCity((String) data.get("city"));
            if (data.containsKey("level")) university.setLevel((String) data.get("level"));
            if (data.containsKey("type")) university.setType((String) data.get("type"));
            if (data.containsKey("description")) university.setIntroduction((String) data.get("description"));
            if (data.containsKey("website")) university.setWebsite((String) data.get("website"));
            if (data.containsKey("phone")) university.setPhone((String) data.get("phone"));
            if (data.containsKey("address")) university.setAddress((String) data.get("address"));
            if (data.containsKey("features")) university.setFeatures((String) data.get("features"));
            if (data.containsKey("ranking")) {
                Object ranking = data.get("ranking");
                if (ranking instanceof Integer) {
                    university.setRanking((Integer) ranking);
                } else if (ranking instanceof String) {
                    try {
                        university.setRanking(Integer.parseInt((String) ranking));
                    } catch (NumberFormatException e) {
                        // 忽略
                    }
                }
            }
            
            // 插入或更新
            if (existing != null) {
                universityMapper.updateById(university);
                log.info("更新院校: {}", name);
            } else {
                universityMapper.insert(university);
                log.info("插入院校: {}", name);
            }
            
            return Result.success("操作成功", Map.of("id", university.getId()));
            
        } catch (Exception e) {
            log.error("插入院校失败", e);
            return Result.error("插入失败: " + e.getMessage());
        }
    }
    
    /**
     * 插入或更新专业数据
     * POST /api/major
     */
    @PostMapping("/major")
    public Result insertMajor(@RequestBody Map<String, Object> data) {
        try {
            String name = (String) data.get("name");
            Object universityIdObj = data.get("university_id");
            
            if (name == null || name.isEmpty()) {
                return Result.error("专业名称不能为空");
            }
            if (universityIdObj == null) {
                return Result.error("院校ID不能为空");
            }
            
            Long universityId;
            if (universityIdObj instanceof Integer) {
                universityId = ((Integer) universityIdObj).longValue();
            } else if (universityIdObj instanceof Long) {
                universityId = (Long) universityIdObj;
            } else {
                universityId = Long.parseLong(universityIdObj.toString());
            }
            
            // 检查是否已存在
            QueryWrapper<Major> wrapper = new QueryWrapper<>();
            wrapper.eq("name", name).eq("university_id", universityId);
            Major existing = majorMapper.selectOne(wrapper);
            
            Major major = existing != null ? existing : new Major();
            
            // 设置字段
            major.setName(name);
            major.setUniversityId(universityId);
            if (data.containsKey("category")) major.setCategory((String) data.get("category"));
            if (data.containsKey("degree")) major.setDegree((String) data.get("degree"));
            if (data.containsKey("description")) major.setIntroduction((String) data.get("description"));
            if (data.containsKey("courses")) major.setCourses((String) data.get("courses"));
            if (data.containsKey("employment_direction")) major.setEmploymentDirection((String) data.get("employment_direction"));
            if (data.containsKey("duration")) {
                Object duration = data.get("duration");
                if (duration instanceof Integer) {
                    major.setDuration((Integer) duration);
                } else if (duration instanceof String) {
                    try {
                        major.setDuration(Integer.parseInt((String) duration));
                    } catch (NumberFormatException e) {
                        // 忽略
                    }
                }
            }
            
            // 插入或更新
            if (existing != null) {
                majorMapper.updateById(major);
                log.info("更新专业: {}", name);
            } else {
                majorMapper.insert(major);
                log.info("插入专业: {}", name);
            }
            
            return Result.success("操作成功", Map.of("id", major.getId()));
            
        } catch (Exception e) {
            log.error("插入专业失败", e);
            return Result.error("插入失败: " + e.getMessage());
        }
    }
}
