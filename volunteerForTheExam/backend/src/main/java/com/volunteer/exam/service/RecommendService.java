package com.volunteer.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.volunteer.exam.entity.Major;
import com.volunteer.exam.entity.University;
import com.volunteer.exam.mapper.MajorMapper;
import com.volunteer.exam.mapper.UniversityMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 智能推荐服务 - 专业级推荐
 */
@Slf4j
@Service
public class RecommendService {

    @Autowired
    private UniversityMapper universityMapper;
    
    @Autowired
    private MajorMapper majorMapper;

    /**
     * 推荐院校和专业（专业级推荐）
     * 
     * @param score 用户分数
     * @param province 用户省份（可为"全国"）
     * @return 推荐列表
     */
    public List<MajorRecommendation> recommendMajors(Integer score, String province) {
        log.info("开始推荐，分数: {}, 省份: {}", score, province);
        
        List<MajorRecommendation> recommendations = new ArrayList<>();
        
        // 1. 根据分数筛选可报考的院校（分数范围：-30 到 +20）
        QueryWrapper<University> uWrapper = new QueryWrapper<>();
        
        // 如果不是"全国"，则按省份筛选
        if (province != null && !"全国".equals(province)) {
            uWrapper.eq("province", province);
        }
        
        List<University> universities = universityMapper.selectList(uWrapper);
        
        // 2. 遍历院校，获取专业并计算录取概率
        for (University university : universities) {
            // 获取该院校的所有专业
            QueryWrapper<Major> mWrapper = new QueryWrapper<>();
            mWrapper.eq("university_id", university.getId());
            List<Major> majors = majorMapper.selectList(mWrapper);
            
            for (Major major : majors) {
                // 3. 计算录取概率（基于院校整体分数线）
                // 注意：这里使用院校的minScore和maxScore作为参考
                // 实际应该查询 admission_score_history 表获取专业历年分数线
                double probability = calculateProbability(score, university);
                
                // 4. 只推荐录取概率大于20%的专业
                if (probability >= 0.2) {
                    MajorRecommendation recommendation = new MajorRecommendation();
                    recommendation.setUniversityId(university.getId());
                    recommendation.setUniversityName(university.getName());
                    recommendation.setMajorId(major.getId());
                    recommendation.setMajorName(major.getName());
                    recommendation.setProvince(university.getProvince());
                    recommendation.setCity(university.getCity());
                    recommendation.setLevel(university.getLevel());
                    recommendation.setType(university.getType());
                    recommendation.setCategory(major.getCategory());
                    recommendation.setProbability(probability);
                    recommendation.setRanking(university.getRanking());
                    
                    // 设置推荐类型
                    if (probability >= 0.8) {
                        recommendation.setRecommendType("保一保");
                    } else if (probability >= 0.5) {
                        recommendation.setRecommendType("稳一稳");
                    } else {
                        recommendation.setRecommendType("冲一冲");
                    }
                    
                    // 模拟历年分数线（实际应从数据库查询）
                    ScoreInfo scoreInfo = new ScoreInfo();
                    scoreInfo.setMinScore(university.getMinScore());
                    scoreInfo.setMaxScore(university.getMaxScore());
                    scoreInfo.setAvgScore((university.getMinScore() + university.getMaxScore()) / 2);
                    recommendation.setLastYearScore(scoreInfo);
                    
                    recommendations.add(recommendation);
                }
            }
        }
        
        // 5. 按录取概率排序（从高到低）
        recommendations.sort(Comparator.comparing(MajorRecommendation::getProbability).reversed());
        
        // 6. 限制返回数量（最多50个）
        if (recommendations.size() > 50) {
            recommendations = recommendations.subList(0, 50);
        }
        
        log.info("推荐完成，共推荐 {} 个专业", recommendations.size());
        return recommendations;
    }

    /**
     * 计算录取概率
     * 
     * @param score 用户分数
     * @param university 院校信息
     * @return 录取概率（0-1之间）
     */
    private double calculateProbability(int score, University university) {
        Integer minScore = university.getMinScore();
        Integer maxScore = university.getMaxScore();
        
        // 如果没有分数线数据，返回中等概率
        if (minScore == null || maxScore == null) {
            return 0.5;
        }
        
        int avgScore = (minScore + maxScore) / 2;
        
        // 根据分数与分数线的关系计算概率
        if (score >= maxScore + 10) {
            return 0.95;  // 远高于最高分，录取概率极高
        } else if (score >= avgScore + 10) {
            return 0.85;  // 高于平均分10分以上，保一保
        } else if (score >= avgScore) {
            return 0.70;  // 高于平均分，稳一稳
        } else if (score >= minScore + 10) {
            return 0.55;  // 高于最低分10分，稳一稳
        } else if (score >= minScore) {
            return 0.40;  // 刚好达到最低分，有希望
        } else if (score >= minScore - 10) {
            return 0.25;  // 低于最低分10分以内，冲一冲
        } else if (score >= minScore - 20) {
            return 0.15;  // 低于最低分20分以内，冲一冲（概率较低）
        } else {
            return 0.05;  // 分数差距太大，不建议报考
        }
    }

    /**
     * 推荐院校（院校级推荐）
     * 
     * @param score 用户分数
     * @param province 用户省份
     * @return 推荐院校列表
     */
    public List<UniversityRecommendation> recommendUniversities(Integer score, String province) {
        log.info("开始推荐院校，分数: {}, 省份: {}", score, province);
        
        List<UniversityRecommendation> recommendations = new ArrayList<>();
        
        // 查询院校
        QueryWrapper<University> wrapper = new QueryWrapper<>();
        if (province != null && !"全国".equals(province)) {
            wrapper.eq("province", province);
        }
        
        List<University> universities = universityMapper.selectList(wrapper);
        
        for (University university : universities) {
            double probability = calculateProbability(score, university);
            
            if (probability >= 0.2) {
                UniversityRecommendation recommendation = new UniversityRecommendation();
                recommendation.setUniversity(university);
                recommendation.setProbability(probability);
                recommendation.setMatchScore((int) (probability * 100));
                
                if (probability >= 0.8) {
                    recommendation.setRecommendType("保一保");
                } else if (probability >= 0.5) {
                    recommendation.setRecommendType("稳一稳");
                } else {
                    recommendation.setRecommendType("冲一冲");
                }
                
                recommendations.add(recommendation);
            }
        }
        
        // 按概率排序
        recommendations.sort(Comparator.comparing(UniversityRecommendation::getProbability).reversed());
        
        // 限制返回数量
        if (recommendations.size() > 30) {
            recommendations = recommendations.subList(0, 30);
        }
        
        return recommendations;
    }

    /**
     * 专业推荐结果
     */
    @Data
    public static class MajorRecommendation {
        private Long universityId;
        private String universityName;
        private Long majorId;
        private String majorName;
        private String province;
        private String city;
        private String level;
        private String type;
        private String category;
        private Double probability;  // 录取概率
        private String recommendType;  // 推荐类型：冲一冲、稳一稳、保一保
        private Integer ranking;  // 院校排名
        private ScoreInfo lastYearScore;  // 去年分数线
        private String majorRanking;  // 专业排名（A+、A等）
        private Double employmentRate;  // 就业率
    }

    /**
     * 院校推荐结果
     */
    @Data
    public static class UniversityRecommendation {
        private University university;
        private Double probability;
        private Integer matchScore;  // 匹配度分数（0-100）
        private String recommendType;
    }

    /**
     * 分数线信息
     */
    @Data
    public static class ScoreInfo {
        private Integer minScore;
        private Integer avgScore;
        private Integer maxScore;
        private Integer minRank;  // 最低位次
    }
}
