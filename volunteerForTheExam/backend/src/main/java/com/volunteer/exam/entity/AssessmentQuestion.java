package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 专业测评问题实体类
 */
@Data
@TableName("assessment_question")
public class AssessmentQuestion {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 问题类别（兴趣、能力、性格、价值观）
     */
    private String category;
    
    /**
     * 问题内容
     */
    private String question;
    
    /**
     * 问题类型（single单选/multiple多选）
     */
    private String questionType;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
