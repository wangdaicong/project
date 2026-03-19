package com.volunteer.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.volunteer.exam.entity.*;
import com.volunteer.exam.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

@Service
public class EmploymentService {
    
    @Autowired
    private MajorEmploymentMapper majorEmploymentMapper;
    
    @Autowired
    private IndustryMapper industryMapper;
    
    @Autowired
    private CareerMapper careerMapper;
    
    @Autowired
    private MajorCareerRelationMapper majorCareerRelationMapper;
    
    @Autowired
    private CityEmploymentMapper cityEmploymentMapper;
    
    @Autowired
    private MajorScoreMapper majorScoreMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public Map<String, Object> getMajorEmploymentData(Long majorId, Integer year) {
        Map<String, Object> result = new HashMap<>();
        
        QueryWrapper<MajorEmployment> wrapper = new QueryWrapper<>();
        wrapper.eq("major_id", majorId);
        if (year != null) {
            wrapper.eq("year", year);
        }
        wrapper.orderByDesc("year");
        
        List<MajorEmployment> employmentList = majorEmploymentMapper.selectList(wrapper);
        
        if (!employmentList.isEmpty()) {
            MajorEmployment latest = employmentList.get(0);
            result.put("employmentRate", latest.getEmploymentRate());
            result.put("avgSalary", latest.getAvgSalary());
            result.put("medianSalary", latest.getMedianSalary());
            result.put("matchRate", latest.getMatchRate());
            result.put("upgradeRate", latest.getUpgradeRate());
            result.put("year", latest.getYear());
            
            try {
                if (latest.getIndustryDistribution() != null) {
                    result.put("industryDistribution", 
                        objectMapper.readValue(latest.getIndustryDistribution(), Map.class));
                }
                if (latest.getTypicalJobs() != null) {
                    result.put("typicalJobs", 
                        objectMapper.readValue(latest.getTypicalJobs(), List.class));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            result.put("educationRequirement", latest.getEducationRequirement());
            result.put("history", employmentList);
        }
        
        return result;
    }
    
    public List<Map<String, Object>> getCareersByMajor(Long majorId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        QueryWrapper<MajorCareerRelation> wrapper = new QueryWrapper<>();
        wrapper.eq("major_id", majorId);
        wrapper.orderByDesc("match_degree");
        
        List<MajorCareerRelation> relations = majorCareerRelationMapper.selectList(wrapper);
        
        for (MajorCareerRelation relation : relations) {
            Career career = careerMapper.selectById(relation.getCareerId());
            if (career != null) {
                Map<String, Object> careerInfo = new HashMap<>();
                careerInfo.put("id", career.getId());
                careerInfo.put("name", career.getName());
                careerInfo.put("avgSalary", career.getAvgSalary());
                careerInfo.put("salaryRange", career.getSalaryRange());
                careerInfo.put("educationRequirement", career.getEducationRequirement());
                careerInfo.put("matchDegree", relation.getMatchDegree());
                careerInfo.put("employmentPercentage", relation.getEmploymentPercentage());
                
                try {
                    if (career.getSkillRequirements() != null) {
                        careerInfo.put("skillRequirements", 
                            objectMapper.readValue(career.getSkillRequirements(), List.class));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                result.add(careerInfo);
            }
        }
        
        return result;
    }
    
    public List<Industry> getAllIndustries() {
        QueryWrapper<Industry> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("avg_salary");
        return industryMapper.selectList(wrapper);
    }
    
    public Map<String, Object> getIndustryDetail(Long industryId) {
        Map<String, Object> result = new HashMap<>();
        
        Industry industry = industryMapper.selectById(industryId);
        if (industry != null) {
            result.put("industry", industry);
            
            QueryWrapper<Career> wrapper = new QueryWrapper<>();
            wrapper.eq("industry_id", industryId);
            wrapper.orderByDesc("avg_salary");
            List<Career> careers = careerMapper.selectList(wrapper);
            result.put("careers", careers);
        }
        
        return result;
    }
    
    public List<CityEmployment> getCityEmploymentData(String tier) {
        QueryWrapper<CityEmployment> wrapper = new QueryWrapper<>();
        if (tier != null && !tier.isEmpty()) {
            wrapper.eq("tier", tier);
        }
        wrapper.orderByDesc("avg_salary");
        return cityEmploymentMapper.selectList(wrapper);
    }
    
    public Map<String, Object> compareMajors(List<Long> majorIds) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> comparisons = new ArrayList<>();
        
        for (Long majorId : majorIds) {
            Map<String, Object> majorData = getMajorEmploymentData(majorId, null);
            comparisons.add(majorData);
        }
        
        result.put("comparisons", comparisons);
        return result;
    }
}
