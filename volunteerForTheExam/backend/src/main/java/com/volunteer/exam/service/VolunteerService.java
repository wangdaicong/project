package com.volunteer.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.volunteer.exam.entity.*;
import com.volunteer.exam.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 志愿填报服务
 */
@Service
public class VolunteerService {
    
    @Autowired
    private VolunteerApplicationMapper applicationMapper;
    
    @Autowired
    private VolunteerDetailMapper detailMapper;
    
    @Autowired
    private VolunteerAnalysisMapper analysisMapper;
    
    @Autowired
    private UniversityMapper universityMapper;
    
    @Autowired
    private ScoreLineMapper scoreLineMapper;
    
    /**
     * 创建志愿填报记录
     */
    @Transactional
    public Long createApplication(VolunteerApplication application) {
        application.setStatus("draft");
        applicationMapper.insert(application);
        return application.getId();
    }
    
    /**
     * 保存志愿详情
     */
    @Transactional
    public void saveVolunteerDetails(Long applicationId, List<VolunteerDetail> details) {
        // 删除旧的志愿详情
        QueryWrapper<VolunteerDetail> wrapper = new QueryWrapper<>();
        wrapper.eq("application_id", applicationId);
        detailMapper.delete(wrapper);
        
        // 插入新的志愿详情
        for (int i = 0; i < details.size(); i++) {
            VolunteerDetail detail = details.get(i);
            detail.setApplicationId(applicationId);
            detail.setVolunteerOrder(i + 1);
            detailMapper.insert(detail);
        }
    }
    
    /**
     * 分析志愿填报方案
     */
    @Transactional
    public Map<String, Object> analyzeVolunteers(Long applicationId) {
        // 获取志愿填报记录
        VolunteerApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            return Collections.emptyMap();
        }
        
        // 获取志愿详情
        List<VolunteerDetail> details = detailMapper.selectByApplicationId(applicationId);
        if (details.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // 分析每个志愿的录取概率和风险等级
        for (VolunteerDetail detail : details) {
            analyzeVolunteerDetail(detail, application);
            detailMapper.updateById(detail);
        }
        
        // 统计分析结果
        VolunteerAnalysis analysis = new VolunteerAnalysis();
        analysis.setApplicationId(applicationId);
        analysis.setTotalVolunteers(details.size());
        
        long rushCount = details.stream().filter(d -> "rush".equals(d.getRiskLevel())).count();
        long stableCount = details.stream().filter(d -> "stable".equals(d.getRiskLevel())).count();
        long safeCount = details.stream().filter(d -> "safe".equals(d.getRiskLevel())).count();
        
        analysis.setRushCount((int) rushCount);
        analysis.setStableCount((int) stableCount);
        analysis.setSafeCount((int) safeCount);
        
        // 计算风险评分
        BigDecimal riskScore = calculateRiskScore(rushCount, stableCount, safeCount, details.size());
        analysis.setRiskScore(riskScore);
        
        // 生成填报建议
        String suggestion = generateSuggestion(rushCount, stableCount, safeCount, riskScore);
        analysis.setSuggestion(suggestion);
        
        // 删除旧的分析结果
        QueryWrapper<VolunteerAnalysis> wrapper = new QueryWrapper<>();
        wrapper.eq("application_id", applicationId);
        analysisMapper.delete(wrapper);
        
        // 保存新的分析结果
        analysisMapper.insert(analysis);
        
        // 更新志愿填报状态为已提交
        application.setStatus("submitted");
        applicationMapper.updateById(application);
        
        // 返回分析结果
        Map<String, Object> result = new HashMap<>();
        result.put("application", application);
        result.put("volunteers", details);
        result.put("analysis", analysis);
        
        return result;
    }
    
    /**
     * 分析单个志愿的录取概率和风险等级
     */
    private void analyzeVolunteerDetail(VolunteerDetail detail, VolunteerApplication application) {
        // 查询该院校的历年分数线
        List<ScoreLine> scoreLines = scoreLineMapper.selectByUniversityAndProvince(
            detail.getUniversityId(),
            application.getProvince(),
            application.getCategory()
        );
        
        if (scoreLines.isEmpty()) {
            detail.setAdmissionProbability("unknown");
            detail.setRiskLevel("stable");
            return;
        }
        
        // 计算平均最低分
        double avgMinScore = scoreLines.stream()
            .mapToInt(ScoreLine::getMinScore)
            .average()
            .orElse(0);
        
        // 计算分数差
        int scoreDiff = application.getScore() - (int) avgMinScore;
        
        // 判断录取概率和风险等级
        if (scoreDiff >= 30) {
            detail.setAdmissionProbability("high");
            detail.setRiskLevel("safe");
        } else if (scoreDiff >= 10) {
            detail.setAdmissionProbability("medium");
            detail.setRiskLevel("stable");
        } else if (scoreDiff >= -10) {
            detail.setAdmissionProbability("medium");
            detail.setRiskLevel("rush");
        } else {
            detail.setAdmissionProbability("low");
            detail.setRiskLevel("rush");
        }
    }
    
    /**
     * 计算风险评分
     */
    private BigDecimal calculateRiskScore(long rushCount, long stableCount, long safeCount, int total) {
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        
        // 风险评分：冲刺志愿占比越高，风险越大
        double rushRatio = (double) rushCount / total;
        double stableRatio = (double) stableCount / total;
        double safeRatio = (double) safeCount / total;
        
        // 理想比例：冲刺30%，稳妥40%，保底30%
        double deviation = Math.abs(rushRatio - 0.3) + Math.abs(stableRatio - 0.4) + Math.abs(safeRatio - 0.3);
        
        // 风险评分 = 偏离度 * 50 + 冲刺占比 * 50
        double score = deviation * 50 + rushRatio * 50;
        
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 生成填报建议
     */
    private String generateSuggestion(long rushCount, long stableCount, long safeCount, BigDecimal riskScore) {
        StringBuilder suggestion = new StringBuilder();
        
        if (riskScore.compareTo(BigDecimal.valueOf(30)) < 0) {
            suggestion.append("✅ 志愿填报方案合理，风险较低。");
        } else if (riskScore.compareTo(BigDecimal.valueOf(60)) < 0) {
            suggestion.append("⚠️ 志愿填报方案风险适中，建议适当调整。");
        } else {
            suggestion.append("❌ 志愿填报方案风险较高，强烈建议调整。");
        }
        
        suggestion.append("\n\n建议：\n");
        
        if (rushCount == 0) {
            suggestion.append("• 缺少冲刺志愿，建议添加1-2个高于自己分数的院校\n");
        } else if (rushCount > 3) {
            suggestion.append("• 冲刺志愿过多，可能导致录取风险增加\n");
        }
        
        if (stableCount < 2) {
            suggestion.append("• 稳妥志愿较少，建议增加2-3个与分数匹配的院校\n");
        }
        
        if (safeCount == 0) {
            suggestion.append("• 缺少保底志愿，强烈建议添加1-2个低于自己分数的院校\n");
        }
        
        suggestion.append("• 建议志愿比例：冲刺30% + 稳妥40% + 保底30%");
        
        return suggestion.toString();
    }
    
    /**
     * 获取志愿填报详情
     */
    public Map<String, Object> getApplicationDetail(Long applicationId) {
        VolunteerApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            return Collections.emptyMap();
        }
        
        List<VolunteerDetail> details = detailMapper.selectByApplicationId(applicationId);
        VolunteerAnalysis analysis = analysisMapper.selectByApplicationId(applicationId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("application", application);
        result.put("volunteers", details);
        result.put("analysis", analysis);
        
        return result;
    }
    
    /**
     * 获取推荐院校列表
     */
    public List<Map<String, Object>> getRecommendedUniversities(String province, Integer score, String category) {
        // 查询所有院校
        List<University> universities = universityMapper.selectList(null);
        
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        for (University university : universities) {
            // 查询该院校的历年分数线
            List<ScoreLine> scoreLines = scoreLineMapper.selectByUniversityAndProvince(
                university.getId(),
                province,
                category
            );
            
            if (scoreLines.isEmpty()) {
                continue;
            }
            
            // 计算平均最低分
            double avgMinScore = scoreLines.stream()
                .mapToInt(ScoreLine::getMinScore)
                .average()
                .orElse(0);
            
            // 计算分数差
            int scoreDiff = score - (int) avgMinScore;
            
            // 判断推荐类型
            String recommendType;
            if (scoreDiff >= 30) {
                recommendType = "safe";
            } else if (scoreDiff >= 10) {
                recommendType = "stable";
            } else if (scoreDiff >= -10) {
                recommendType = "rush";
            } else {
                continue; // 分数差太大，不推荐
            }
            
            Map<String, Object> recommendation = new HashMap<>();
            recommendation.put("university", university);
            recommendation.put("avgMinScore", (int) avgMinScore);
            recommendation.put("scoreDiff", scoreDiff);
            recommendation.put("recommendType", recommendType);
            
            recommendations.add(recommendation);
        }
        
        // 按分数差排序
        recommendations.sort((a, b) -> {
            Integer diffA = (Integer) a.get("scoreDiff");
            Integer diffB = (Integer) b.get("scoreDiff");
            return diffB.compareTo(diffA);
        });
        
        return recommendations;
    }
    
    /**
     * 获取用户的志愿填报记录列表
     */
    public List<VolunteerApplication> getUserApplications(Long userId) {
        QueryWrapper<VolunteerApplication> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("created_time");
        return applicationMapper.selectList(wrapper);
    }
}
