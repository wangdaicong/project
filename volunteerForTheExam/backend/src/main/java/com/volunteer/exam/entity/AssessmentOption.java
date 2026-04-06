package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 专业测评选项实体类
 */
@Data
@TableName("assessment_option")
public class AssessmentOption {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 问题ID
     */
    private Long questionId;
    
    /**
     * 选项内容
     */
    private String optionText;
    
    /**
     * 选项分数
     */
    private Integer score;
    
    /**
     * 关联专业标签（逗号分隔）
     */
    private String majorTags;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
