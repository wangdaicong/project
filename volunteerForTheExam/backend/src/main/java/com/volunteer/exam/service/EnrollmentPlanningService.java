package com.volunteer.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.volunteer.exam.entity.EnrollmentPath;
import com.volunteer.exam.mapper.EnrollmentPathMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

@Service
public class EnrollmentPlanningService {
    
    @Autowired
    private EnrollmentPathMapper enrollmentPathMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public List<EnrollmentPath> getAllPaths() {
        return enrollmentPathMapper.selectList(null);
    }
    
    public Map<String, Object> getPathDetail(Long pathId) {
        Map<String, Object> result = new HashMap<>();
        
        EnrollmentPath path = enrollmentPathMapper.selectById(pathId);
        if (path != null) {
            result.put("id", path.getId());
            result.put("name", path.getName());
            result.put("type", path.getType());
            result.put("description", path.getDescription());
            result.put("requirements", path.getRequirements());
            result.put("advantages", path.getAdvantages());
            result.put("disadvantages", path.getDisadvantages());
            result.put("suitableStudents", path.getSuitableStudents());
            
            try {
                if (path.getUniversities() != null) {
                    result.put("universities", 
                        objectMapper.readValue(path.getUniversities(), List.class));
                }
                if (path.getMajors() != null) {
                    result.put("majors", 
                        objectMapper.readValue(path.getMajors(), List.class));
                }
                if (path.getTimeline() != null) {
                    result.put("timeline", 
                        objectMapper.readValue(path.getTimeline(), List.class));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return result;
    }
    
    public List<Map<String, Object>> recommendPaths(Map<String, Object> studentProfile) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        Integer score = (Integer) studentProfile.get("score");
        List<String> interests = (List<String>) studentProfile.get("interests");
        
        List<EnrollmentPath> allPaths = getAllPaths();
        
        for (EnrollmentPath path : allPaths) {
            Map<String, Object> recommendation = new HashMap<>();
            recommendation.put("path", path);
            
            int matchScore = calculateMatchScore(path, score, interests);
            recommendation.put("matchScore", matchScore);
            recommendation.put("suitable", matchScore >= 60);
            
            recommendations.add(recommendation);
        }
        
        recommendations.sort((a, b) -> 
            Integer.compare((Integer)b.get("matchScore"), (Integer)a.get("matchScore")));
        
        return recommendations;
    }
    
    private int calculateMatchScore(EnrollmentPath path, Integer score, List<String> interests) {
        int matchScore = 50;
        
        if ("强基计划".equals(path.getName()) && score != null && score >= 650) {
            matchScore += 30;
        } else if ("综合评价".equals(path.getName()) && score != null && score >= 600) {
            matchScore += 25;
        } else if ("专升本".equals(path.getName())) {
            matchScore += 20;
        }
        
        if (interests != null && !interests.isEmpty()) {
            matchScore += 10;
        }
        
        return Math.min(matchScore, 100);
    }
}
