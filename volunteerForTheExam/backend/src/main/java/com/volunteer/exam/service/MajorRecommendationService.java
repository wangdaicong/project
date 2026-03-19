package com.volunteer.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.volunteer.exam.entity.*;
import com.volunteer.exam.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MajorRecommendationService {
    
    @Autowired
    private MajorMapper majorMapper;
    
    @Autowired
    private MajorEmploymentMapper majorEmploymentMapper;
    
    @Autowired
    private MajorScoreMapper majorScoreMapper;
    
    @Autowired
    private MajorCareerRelationMapper majorCareerRelationMapper;
    
    @Autowired
    private CareerMapper careerMapper;
    
    public List<Map<String, Object>> recommendByEmployment(Map<String, Object> criteria) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        String category = (String) criteria.get("category");
        Integer minSalary = (Integer) criteria.get("minSalary");
        
        QueryWrapper<Major> majorWrapper = new QueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            majorWrapper.eq("category", category);
        }
        List<Major> majors = majorMapper.selectList(majorWrapper);
        
        for (Major major : majors) {
            QueryWrapper<MajorEmployment> empWrapper = new QueryWrapper<>();
            empWrapper.eq("major_id", major.getId());
            empWrapper.orderByDesc("year");
            empWrapper.last("LIMIT 1");
            
            MajorEmployment employment = majorEmploymentMapper.selectOne(empWrapper);
            
            if (employment != null) {
                if (minSalary == null || employment.getAvgSalary() >= minSalary) {
                    Map<String, Object> recommendation = new HashMap<>();
                    recommendation.put("major", major);
                    recommendation.put("employment", employment);
                    
                    int score = calculateEmploymentScore(employment);
                    recommendation.put("score", score);
                    
                    recommendations.add(recommendation);
                }
            }
        }
        
        recommendations.sort((a, b) -> 
            Integer.compare((Integer)b.get("score"), (Integer)a.get("score")));
        
        return recommendations.stream().limit(20).collect(Collectors.toList());
    }
    
    public List<Map<String, Object>> recommendByCareer(String careerName) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        QueryWrapper<Career> careerWrapper = new QueryWrapper<>();
        careerWrapper.like("name", careerName);
        List<Career> careers = careerMapper.selectList(careerWrapper);
        
        for (Career career : careers) {
            QueryWrapper<MajorCareerRelation> relationWrapper = new QueryWrapper<>();
            relationWrapper.eq("career_id", career.getId());
            relationWrapper.orderByDesc("match_degree");
            
            List<MajorCareerRelation> relations = majorCareerRelationMapper.selectList(relationWrapper);
            
            for (MajorCareerRelation relation : relations) {
                Major major = majorMapper.selectById(relation.getMajorId());
                if (major != null) {
                    Map<String, Object> recommendation = new HashMap<>();
                    recommendation.put("major", major);
                    recommendation.put("career", career);
                    recommendation.put("matchDegree", relation.getMatchDegree());
                    recommendation.put("employmentPercentage", relation.getEmploymentPercentage());
                    
                    recommendations.add(recommendation);
                }
            }
        }
        
        return recommendations;
    }
    
    public Map<String, Object> getMajorScore(Long majorId, Integer year) {
        Map<String, Object> result = new HashMap<>();
        
        QueryWrapper<MajorScore> wrapper = new QueryWrapper<>();
        wrapper.eq("major_id", majorId);
        if (year != null) {
            wrapper.eq("year", year);
        }
        wrapper.orderByDesc("year");
        wrapper.last("LIMIT 1");
        
        MajorScore score = majorScoreMapper.selectOne(wrapper);
        
        if (score != null) {
            result.put("employmentScore", score.getEmploymentScore());
            result.put("salaryScore", score.getSalaryScore());
            result.put("developmentScore", score.getDevelopmentScore());
            result.put("stabilityScore", score.getStabilityScore());
            result.put("totalScore", score.getTotalScore());
            result.put("recommendationLevel", score.getRecommendationLevel());
            result.put("year", score.getYear());
        } else {
            QueryWrapper<MajorEmployment> empWrapper = new QueryWrapper<>();
            empWrapper.eq("major_id", majorId);
            empWrapper.orderByDesc("year");
            empWrapper.last("LIMIT 1");
            
            MajorEmployment employment = majorEmploymentMapper.selectOne(empWrapper);
            if (employment != null) {
                int calculatedScore = calculateEmploymentScore(employment);
                result.put("totalScore", calculatedScore);
                result.put("recommendationLevel", getRecommendationLevel(calculatedScore));
                result.put("calculated", true);
            }
        }
        
        return result;
    }
    
    private int calculateEmploymentScore(MajorEmployment employment) {
        int score = 0;
        
        if (employment.getEmploymentRate() != null) {
            score += employment.getEmploymentRate().intValue() * 0.3;
        }
        
        if (employment.getAvgSalary() != null) {
            int salaryScore = Math.min(employment.getAvgSalary() / 100, 40);
            score += salaryScore * 0.4;
        }
        
        if (employment.getMatchRate() != null) {
            score += employment.getMatchRate().intValue() * 0.3;
        }
        
        return Math.min(score, 100);
    }
    
    private String getRecommendationLevel(int score) {
        if (score >= 80) return "强烈推荐";
        if (score >= 60) return "推荐";
        if (score >= 40) return "谨慎";
        return "不推荐";
    }
}
