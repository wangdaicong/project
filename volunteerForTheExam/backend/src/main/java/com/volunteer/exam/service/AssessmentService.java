package com.volunteer.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volunteer.exam.entity.AssessmentOption;
import com.volunteer.exam.entity.AssessmentQuestion;
import com.volunteer.exam.entity.AssessmentRecord;
import com.volunteer.exam.mapper.AssessmentOptionMapper;
import com.volunteer.exam.mapper.AssessmentQuestionMapper;
import com.volunteer.exam.mapper.AssessmentRecordMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 专业测评服务
 */
@Slf4j
@Service
public class AssessmentService {
    
    @Resource
    private AssessmentQuestionMapper questionMapper;
    
    @Resource
    private AssessmentOptionMapper optionMapper;
    
    @Resource
    private AssessmentRecordMapper recordMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 获取测评问卷
     */
    public Map<String, Object> getAssessmentQuestionnaire() {
        // 查询所有问题，按排序
        LambdaQueryWrapper<AssessmentQuestion> questionWrapper = new LambdaQueryWrapper<>();
        questionWrapper.orderByAsc(AssessmentQuestion::getSortOrder);
        List<AssessmentQuestion> questions = questionMapper.selectList(questionWrapper);
        
        // 查询所有选项
        List<AssessmentOption> allOptions = optionMapper.selectList(null);
        Map<Long, List<AssessmentOption>> optionsMap = allOptions.stream()
                .collect(Collectors.groupingBy(AssessmentOption::getQuestionId));
        
        // 组装问卷数据
        List<Map<String, Object>> questionnaireData = new ArrayList<>();
        for (AssessmentQuestion question : questions) {
            Map<String, Object> questionData = new HashMap<>();
            questionData.put("id", question.getId());
            questionData.put("category", question.getCategory());
            questionData.put("question", question.getQuestion());
            questionData.put("questionType", question.getQuestionType());
            
            // 获取该问题的选项
            List<AssessmentOption> options = optionsMap.getOrDefault(question.getId(), new ArrayList<>());
            options.sort(Comparator.comparing(AssessmentOption::getSortOrder));
            
            List<Map<String, Object>> optionList = new ArrayList<>();
            for (AssessmentOption option : options) {
                Map<String, Object> optionData = new HashMap<>();
                optionData.put("id", option.getId());
                optionData.put("text", option.getOptionText());
                optionData.put("score", option.getScore());
                optionList.add(optionData);
            }
            questionData.put("options", optionList);
            
            questionnaireData.add(questionData);
        }
        
        // 按类别分组
        Map<String, List<Map<String, Object>>> groupedQuestions = questionnaireData.stream()
                .collect(Collectors.groupingBy(q -> (String) q.get("category")));
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", questions.size());
        result.put("questions", questionnaireData);
        result.put("categories", Arrays.asList("兴趣", "能力", "性格", "价值观"));
        result.put("groupedQuestions", groupedQuestions);
        
        return result;
    }
    
    /**
     * 提交测评并生成推荐
     */
    public Map<String, Object> submitAssessment(Map<String, Object> answers, Long userId) {
        try {
            // 1. 统计各专业标签的得分
            Map<String, Integer> majorScores = new HashMap<>();
            Map<String, Integer> categoryScores = new HashMap<>();
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> answerList = (List<Map<String, Object>>) answers.get("answers");
            
            for (Map<String, Object> answer : answerList) {
                Long questionId = Long.valueOf(answer.get("questionId").toString());
                Long optionId = Long.valueOf(answer.get("optionId").toString());
                
                // 获取问题信息
                AssessmentQuestion question = questionMapper.selectById(questionId);
                if (question == null) continue;
                
                // 获取选项信息
                AssessmentOption option = optionMapper.selectById(optionId);
                if (option == null) continue;
                
                // 累加类别得分
                String category = question.getCategory();
                categoryScores.put(category, categoryScores.getOrDefault(category, 0) + option.getScore());
                
                // 累加专业标签得分
                if (option.getMajorTags() != null && !option.getMajorTags().isEmpty()) {
                    String[] tags = option.getMajorTags().split(",");
                    for (String tag : tags) {
                        tag = tag.trim();
                        majorScores.put(tag, majorScores.getOrDefault(tag, 0) + option.getScore());
                    }
                }
            }
            
            // 2. 排序并获取推荐专业（前10个）
            List<Map<String, Object>> recommendedMajors = majorScores.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .limit(10)
                    .map(entry -> {
                        Map<String, Object> major = new HashMap<>();
                        major.put("name", entry.getKey());
                        major.put("score", entry.getValue());
                        major.put("matchRate", Math.min(100, (entry.getValue() * 100) / 200)); // 假设满分200
                        return major;
                    })
                    .collect(Collectors.toList());
            
            // 3. 保存测评记录
            AssessmentRecord record = new AssessmentRecord();
            record.setUserId(userId);
            record.setAnswers(objectMapper.writeValueAsString(answerList));
            record.setResultScores(objectMapper.writeValueAsString(categoryScores));
            record.setRecommendedMajors(objectMapper.writeValueAsString(recommendedMajors));
            recordMapper.insert(record);
            
            // 4. 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("recordId", record.getId());
            result.put("categoryScores", categoryScores);
            result.put("recommendedMajors", recommendedMajors);
            result.put("totalQuestions", answerList.size());
            
            return result;
            
        } catch (Exception e) {
            log.error("提交测评失败", e);
            throw new RuntimeException("提交测评失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取测评记录
     */
    public AssessmentRecord getAssessmentRecord(Long recordId) {
        return recordMapper.selectById(recordId);
    }
    
    /**
     * 获取用户的测评历史
     */
    public List<AssessmentRecord> getUserAssessmentHistory(Long userId) {
        LambdaQueryWrapper<AssessmentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssessmentRecord::getUserId, userId);
        wrapper.orderByDesc(AssessmentRecord::getCreatedTime);
        return recordMapper.selectList(wrapper);
    }
}
