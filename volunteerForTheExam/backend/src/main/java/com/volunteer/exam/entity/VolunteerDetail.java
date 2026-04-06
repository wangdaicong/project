package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 志愿详情实体类
 */
@Data
@TableName("volunteer_detail")
public class VolunteerDetail {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long applicationId;
    
    private Integer volunteerOrder;
    
    private Long universityId;
    
    private String universityName;
    
    private Long majorId;
    
    private String majorName;
    
    private String admissionProbability;
    
    private String riskLevel;
    
    private LocalDateTime createdTime;
}
