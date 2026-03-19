package com.volunteer.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volunteer.exam.entity.Major;
import com.volunteer.exam.mapper.MajorMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MajorService {
    
    @Resource
    private MajorMapper majorMapper;

    public List<Major> getByUniversityId(Long universityId) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Major::getUniversityId, universityId)
               .orderByDesc(Major::getEmploymentRate);
        return majorMapper.selectList(wrapper);
    }

    public Major getById(Long id) {
        return majorMapper.selectById(id);
    }

    public List<Major> queryByCategory(String category) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(category)) {
            wrapper.eq(Major::getCategory, category);
        }
        wrapper.orderByDesc(Major::getEmploymentRate);
        return majorMapper.selectList(wrapper);
    }

    public List<Major> searchMajors(String keyword) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Major::getName, keyword)
               .or()
               .like(Major::getCategory, keyword)
               .orderByDesc(Major::getEmploymentRate);
        return majorMapper.selectList(wrapper);
    }
}
