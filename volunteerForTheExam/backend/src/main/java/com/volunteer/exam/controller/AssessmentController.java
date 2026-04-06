package com.volunteer.exam.controller;

import com.volunteer.exam.common.Result;
import com.volunteer.exam.entity.AssessmentRecord;
import com.volunteer.exam.service.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 专业测评控制器
 */
@RestController
@RequestMapping("/api/assessment")
public class AssessmentController {
    
    @Autowired
    private AssessmentService assessmentService;
    
    /**
     * 获取测评问卷
     * 
     * @return 问卷数据
     */
    @GetMapping("/questionnaire")
    public Result<Map<String, Object>> getQuestionnaire() {
        Map<String, Object> questionnaire = assessmentService.getAssessmentQuestionnaire();
        return Result.success("获取成功", questionnaire);
    }
    
    /**
     * 提交测评答案
     * 
     * @param params 答案数据
     * @return 测评结果
     */
    @PostMapping("/submit")
    public Result<Map<String, Object>> submitAssessment(@RequestBody Map<String, Object> params) {
        Long userId = params.get("userId") != null ? 
                Long.valueOf(params.get("userId").toString()) : null;
        
        Map<String, Object> result = assessmentService.submitAssessment(params, userId);
        return Result.success("测评完成", result);
    }
    
    /**
     * 获取测评记录
     * 
     * @param recordId 记录ID
     * @return 测评记录
     */
    @GetMapping("/record/{recordId}")
    public Result<AssessmentRecord> getRecord(@PathVariable Long recordId) {
        AssessmentRecord record = assessmentService.getAssessmentRecord(recordId);
        if (record == null) {
            return Result.error("记录不存在");
        }
        return Result.success("获取成功", record);
    }
    
    /**
     * 获取用户测评历史
     * 
     * @param userId 用户ID
     * @return 测评历史列表
     */
    @GetMapping("/history/{userId}")
    public Result<List<AssessmentRecord>> getHistory(@PathVariable Long userId) {
        List<AssessmentRecord> history = assessmentService.getUserAssessmentHistory(userId);
        return Result.success("获取成功", history);
    }
}
