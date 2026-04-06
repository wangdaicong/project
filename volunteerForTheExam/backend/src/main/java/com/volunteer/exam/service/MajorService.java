package com.volunteer.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.exam.entity.Major;
import com.volunteer.exam.entity.UniversityMajor;
import com.volunteer.exam.mapper.MajorMapper;
import com.volunteer.exam.mapper.UniversityMajorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class MajorService {
    
    @Resource
    private MajorMapper majorMapper;
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private UniversityMajorMapper universityMajorMapper;

    public List<Major> getByUniversityId(Long universityId) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Major::getUniversityId, universityId)
               .orderByDesc(Major::getEmploymentRate);
        return majorMapper.selectList(wrapper);
    }

    public Major getById(Long id) {
        return majorMapper.selectById(id);
    }

    @SuppressWarnings("unchecked")
    public List<Major> queryByCategory(String category) {
        String cacheKey = "majors_by_category:" + (category != null ? category : "all");
        
        Object cached = cacheService.get(cacheKey);
        if (cached != null) {
            return (List<Major>) cached;
        }
        
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(category)) {
            wrapper.eq(Major::getCategory, category);
        }
        wrapper.orderByDesc(Major::getEmploymentRate);
        List<Major> majors = majorMapper.selectList(wrapper);
        
        cacheService.set(cacheKey, majors, 1, TimeUnit.HOURS);
        
        return majors;
    }

    public List<Major> searchMajors(String keyword) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Major::getName, keyword)
               .or()
               .like(Major::getCategory, keyword)
               .orderByDesc(Major::getEmploymentRate);
        return majorMapper.selectList(wrapper);
    }
    
    public Page<Major> getMajorList(String keyword, String category, String subCategory, 
                                     String degreeLevel, Integer page, Integer size) {
        Page<Major> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Major::getName, keyword)
                             .or().like(Major::getCategory, keyword));
        }
        
        if (StringUtils.hasText(category)) {
            wrapper.eq(Major::getCategory, category);
        }
        
        if (StringUtils.hasText(subCategory)) {
            wrapper.like(Major::getCategory, subCategory);
        }
        
        if (StringUtils.hasText(degreeLevel)) {
            wrapper.eq(Major::getDegree, degreeLevel);
        }
        
        wrapper.orderByDesc(Major::getEmploymentRate);
        
        return majorMapper.selectPage(pageObj, wrapper);
    }
    
    public List<String> getCategories() {
        QueryWrapper<Major> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT category");
        List<Major> majors = majorMapper.selectList(wrapper);
        return majors.stream()
                     .map(Major::getCategory)
                     .filter(Objects::nonNull)
                     .distinct()
                     .collect(Collectors.toList());
    }
    
    public List<String> getSubCategories(String category) {
        QueryWrapper<Major> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT category");
        if (StringUtils.hasText(category)) {
            wrapper.eq("category", category);
        }
        List<Major> majors = majorMapper.selectList(wrapper);
        return majors.stream()
                     .map(Major::getCategory)
                     .filter(Objects::nonNull)
                     .distinct()
                     .collect(Collectors.toList());
    }
    
    public Map<String, Object> getUniversitiesByMajor(Long majorId, String province, String level) {
        QueryWrapper<UniversityMajor> wrapper = new QueryWrapper<>();
        wrapper.eq("major_id", majorId);
        
        if (StringUtils.hasText(province)) {
            wrapper.eq("province", province);
        }
        
        List<UniversityMajor> universityMajors = universityMajorMapper.selectList(wrapper);
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", universityMajors.size());
        result.put("universities", universityMajors);
        
        return result;
    }
}
