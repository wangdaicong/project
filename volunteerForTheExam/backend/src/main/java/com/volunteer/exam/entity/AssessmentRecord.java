package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 专业测评记录实体类
 */
@Data
@TableName("assessment_record")
public class AssessmentRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 答案JSON
     */
    private String answers;
    
    /**
     * 各类别得分JSON
     */
    private String resultScores;
    
    /**
     * 推荐专业JSON
     */
    private String recommendedMajors;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
