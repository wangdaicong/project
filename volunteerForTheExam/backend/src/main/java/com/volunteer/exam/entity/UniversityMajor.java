package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("university_major")
public class UniversityMajor implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("university_id")
    private Long universityId;
    
    @TableField("major_id")
    private Long majorId;
    
    @TableField("department_id")
    private Long departmentId;
    
    @TableField("is_featured")
    private Boolean isFeatured;
    
    @TableField("feature_level")
    private String featureLevel;
    
    @TableField("enrollment_plan")
    private Integer enrollmentPlan;
    
    @TableField("min_score")
    private Integer minScore;
    
    @TableField("avg_score")
    private Integer avgScore;
    
    @TableField("max_score")
    private Integer maxScore;
    
    private Integer year;
    
    private String province;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
}
