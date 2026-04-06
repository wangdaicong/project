package com.volunteer.exam.service;

import com.volunteer.exam.entity.ScoreLine;
import com.volunteer.exam.mapper.ScoreLineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 历年分数线服务
 */
@Service
public class ScoreLineService {
    
    @Autowired
    private ScoreLineMapper scoreLineMapper;
    
    /**
     * 获取某院校的历年分数线趋势
     */
    public Map<String, Object> getUniversityScoreTrend(Long universityId, String province, String category) {
        List<ScoreLine> scoreLines = scoreLineMapper.selectByUniversityAndProvince(universityId, province, category);
        
        if (scoreLines.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // 按年份倒序排列
        scoreLines.sort((a, b) -> b.getYear().compareTo(a.getYear()));
        
        Map<String, Object> result = new HashMap<>();
        result.put("universityName", scoreLines.get(0).getUniversityName());
        result.put("province", province);
        result.put("category", category);
        
        // 提取年份和分数数据
        List<Integer> years = scoreLines.stream()
                .map(ScoreLine::getYear)
                .collect(Collectors.toList());
        
        List<Integer> minScores = scoreLines.stream()
                .map(ScoreLine::getMinScore)
                .collect(Collectors.toList());
        
        List<Integer> avgScores = scoreLines.stream()
                .map(ScoreLine::getAvgScore)
                .collect(Collectors.toList());
        
        List<Integer> maxScores = scoreLines.stream()
                .map(ScoreLine::getMaxScore)
                .collect(Collectors.toList());
        
        List<Integer> minRanks = scoreLines.stream()
                .map(ScoreLine::getMinRank)
                .collect(Collectors.toList());
        
        result.put("years", years);
        result.put("minScores", minScores);
        result.put("avgScores", avgScores);
        result.put("maxScores", maxScores);
        result.put("minRanks", minRanks);
        result.put("data", scoreLines);
        
        return result;
    }
    
    /**
     * 获取某省份某年份的分数线排名
     */
    public List<ScoreLine> getProvinceYearRanking(String province, Integer year, String category) {
        return scoreLineMapper.selectByProvinceAndYear(province, year, category);
    }
    
    /**
     * 获取可用的年份列表
     */
    public List<Integer> getAvailableYears() {
        return scoreLineMapper.selectAvailableYears();
    }
    
    /**
     * 对比多个院校的分数线趋势
     */
    public Map<String, Object> compareUniversities(List<Long> universityIds, String province, String category) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> universities = new ArrayList<>();
        
        for (Long universityId : universityIds) {
            Map<String, Object> trend = getUniversityScoreTrend(universityId, province, category);
            if (!trend.isEmpty()) {
                universities.add(trend);
            }
        }
        
        result.put("universities", universities);
        result.put("province", province);
        result.put("category", category);
        
        return result;
    }
}
