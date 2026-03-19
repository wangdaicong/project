package com.volunteer.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.exam.entity.University;
import com.volunteer.exam.entity.AdmissionRecord;
import com.volunteer.exam.mapper.UniversityMapper;
import com.volunteer.exam.mapper.AdmissionRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UniversityService {
    
    @Resource
    private UniversityMapper universityMapper;
    
    @Resource
    private AdmissionRecordMapper admissionRecordMapper;

    public IPage<University> queryUniversities(Integer pageNum, Integer pageSize, 
                                               String province, Integer minScore, 
                                               Integer maxScore, String level, String type) {
        Page<University> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<University> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(province)) {
            wrapper.eq(University::getProvince, province);
        }
        if (StringUtils.hasText(level)) {
            // 使用模糊匹配而不是精确匹配，因为数据库中是 "985/211/双一流" 格式
            wrapper.like(University::getLevel, level);
        }
        if (StringUtils.hasText(type)) {
            wrapper.like(University::getType, type);
        }
        if (minScore != null && maxScore != null) {
            wrapper.le(University::getMinScore, maxScore)
                   .ge(University::getMaxScore, minScore);
        }
        
        wrapper.orderByAsc(University::getRanking);
        return universityMapper.selectPage(page, wrapper);
    }

    public University getById(Long id) {
        return universityMapper.selectById(id);
    }

    public List<University> getRecommendations(Integer score, String province) {
        LambdaQueryWrapper<University> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(University::getMinScore, score + 20)
               .ge(University::getMinScore, score - 30);
        
        if (StringUtils.hasText(province)) {
            wrapper.eq(University::getProvince, province);
        }
        
        wrapper.orderByAsc(University::getRanking).last("limit 20");
        return universityMapper.selectList(wrapper);
    }

    /**
     * 获取分类热门院校（首页展示）
     * 返回Map: {"985": [...], "211": [...], "专科": [...]}
     */
    public Map<String, List<Map<String, Object>>> getHotUniversitiesByCategory() {
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        
        // 1. 获取985前3所（排名最高）
        LambdaQueryWrapper<University> wrapper985 = new LambdaQueryWrapper<>();
        wrapper985.like(University::getLevel, "985")
                  .isNotNull(University::getRanking)
                  .orderByAsc(University::getRanking)
                  .last("limit 3");
        List<University> top985 = universityMapper.selectList(wrapper985);
        result.put("985", enrichUniversitiesWithScores(top985));
        
        // 2. 获取211前3所（排除985，排名最高）
        LambdaQueryWrapper<University> wrapper211 = new LambdaQueryWrapper<>();
        wrapper211.like(University::getLevel, "211")
                  .notLike(University::getLevel, "985")
                  .isNotNull(University::getRanking)
                  .orderByAsc(University::getRanking)
                  .last("limit 3");
        List<University> top211 = universityMapper.selectList(wrapper211);
        result.put("211", enrichUniversitiesWithScores(top211));
        
        // 3. 获取专科前3所（按名称）
        LambdaQueryWrapper<University> wrapperVocational = new LambdaQueryWrapper<>();
        wrapperVocational.like(University::getLevel, "专科")
                         .last("limit 3");
        List<University> topVocational = universityMapper.selectList(wrapperVocational);
        result.put("专科", enrichUniversitiesWithScores(topVocational));
        
        return result;
    }
    
    /**
     * 为院校列表补充最新分数线信息
     */
    private List<Map<String, Object>> enrichUniversitiesWithScores(List<University> universities) {
        return universities.stream().map(university -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", university.getId());
            map.put("name", university.getName());
            map.put("province", university.getProvince());
            map.put("city", university.getCity());
            map.put("level", university.getLevel());
            map.put("type", university.getType());
            map.put("ranking", university.getRanking());
            map.put("logoUrl", university.getLogoUrl());
            
            // 查询最新分数线（2023年）
            LambdaQueryWrapper<AdmissionRecord> scoreWrapper = new LambdaQueryWrapper<>();
            scoreWrapper.eq(AdmissionRecord::getUniversityId, university.getId())
                       .eq(AdmissionRecord::getYear, 2023)
                       .orderByDesc(AdmissionRecord::getMinScore)
                       .last("limit 1");
            AdmissionRecord latestScore = admissionRecordMapper.selectOne(scoreWrapper);
            
            if (latestScore != null) {
                map.put("minScore", latestScore.getMinScore());
                map.put("maxScore", latestScore.getMaxScore());
                map.put("avgScore", latestScore.getAvgScore());
            } else {
                map.put("minScore", null);
                map.put("maxScore", null);
                map.put("avgScore", null);
            }
            
            return map;
        }).collect(Collectors.toList());
    }
}
