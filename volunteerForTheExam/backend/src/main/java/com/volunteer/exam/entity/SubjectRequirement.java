package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 专业选科要求实体类
 */
@Data
@TableName("major_subject_requirement")
public class SubjectRequirement {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private String province;
    
    private Integer year;
    
    private Integer universityId;
    
    private String universityName;
    
    private String majorCode;
    
    private String majorName;
    
    private String majorCategory;
    
    private String subjectRequirement;
    
    private Integer requirementType;
    
    private String subjects;
    
    private String degreeLevel;
    
    private Integer canApply;
    
    private Date createdAt;
    
    private Date updatedAt;
}
