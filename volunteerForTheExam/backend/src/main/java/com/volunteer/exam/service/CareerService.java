package com.volunteer.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.volunteer.exam.entity.Career;
import com.volunteer.exam.mapper.CareerMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CareerService {
    
    @Resource
    private CareerMapper careerMapper;

    public List<Career> queryByIndustryId(Long industryId) {
        LambdaQueryWrapper<Career> wrapper = new LambdaQueryWrapper<>();
        if (industryId != null) {
            wrapper.eq(Career::getIndustryId, industryId);
        }
        wrapper.orderByDesc(Career::getAvgSalary);
        return careerMapper.selectList(wrapper);
    }

    public Career getById(Long id) {
        return careerMapper.selectById(id);
    }

    public List<Career> getHotCareers() {
        LambdaQueryWrapper<Career> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Career::getAvgSalary).last("limit 10");
        return careerMapper.selectList(wrapper);
    }

    public List<Career> searchCareers(String keyword) {
        QueryWrapper<Career> wrapper = new QueryWrapper<>();
        wrapper.like("name", keyword)
               .or()
               .like("description", keyword)
               .orderBy(true, false, "avg_salary");
        return careerMapper.selectList(wrapper);
    }
}
