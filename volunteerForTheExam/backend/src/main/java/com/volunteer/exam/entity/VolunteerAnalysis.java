package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 志愿分析结果实体类
 */
@Data
@TableName("volunteer_analysis")
public class VolunteerAnalysis {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long applicationId;
    
    private Integer totalVolunteers;
    
    private Integer rushCount;
    
    private Integer stableCount;
    
    private Integer safeCount;
    
    private BigDecimal riskScore;
    
    private String suggestion;
    
    private LocalDateTime createdTime;
}
