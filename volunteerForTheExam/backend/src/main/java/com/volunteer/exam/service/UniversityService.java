package com.volunteer.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.exam.entity.*;
import com.volunteer.exam.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UniversityService {
    
    @Autowired
    private UniversityMapper universityMapper;
    
    @Autowired
    private UniversityMajorMapper universityMajorMapper;
    
    public Page<University> getUniversityList(String keyword, String province, String type, 
                                               String level, Integer page, Integer size) {
        Page<University> pageObj = new Page<>(page, size);
        QueryWrapper<University> wrapper = new QueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("school_name", keyword)
                             .or().like("school_id_code", keyword));
        }
        
        if (province != null && !province.isEmpty()) {
            wrapper.like("location", province);
        }
        
        if (type != null && !type.isEmpty()) {
            wrapper.like("school_level", type);
        }
        
        if (level != null && !level.isEmpty()) {
            if ("985".equals(level)) {
                wrapper.eq("is_985", true);
            } else if ("211".equals(level)) {
                wrapper.eq("is_211", true);
            } else if ("双一流".equals(level)) {
                wrapper.eq("is_double_first_class", true);
            }
        }
        
        wrapper.orderByDesc("is_985", "is_211", "is_double_first_class");
        
        return universityMapper.selectPage(pageObj, wrapper);
    }
    
    public University getUniversityDetail(Long id) {
        return universityMapper.selectById(id);
    }
    
    public Map<String, Object> getUniversityMajors(Long universityId, String category, String degreeLevel) {
        QueryWrapper<UniversityMajor> wrapper = new QueryWrapper<>();
        wrapper.eq("university_id", universityId);
        
        if (category != null && !category.isEmpty()) {
            wrapper.eq("category", category);
        }
        
        if (degreeLevel != null && !degreeLevel.isEmpty()) {
            wrapper.eq("degree_level", degreeLevel);
        }
        
        wrapper.orderBy(true, true, "category", "major_name");
        
        List<UniversityMajor> universityMajors = universityMajorMapper.selectList(wrapper);
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", universityMajors.size());
        result.put("majors", universityMajors);
        
        return result;
    }
}
