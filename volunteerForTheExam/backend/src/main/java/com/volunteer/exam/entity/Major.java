package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("major")
public class Major implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long universityId;
    private String name;
    private String category;
    private String majorCategory;
    private String degree;
    private String introduction;
    private String courses;
    private String employmentDirection;
    private Double employmentRate;
    private Integer duration;
    private Integer minScore;
    private Integer enrollmentNumber;
    private String tags;
    
    // 张雪峰式字段
    private String zhangxuefengTags;
    private String civilServiceAdvantage;
    private String postgraduateNecessity;
    private String employmentReality;
    private String commonMisconceptions;
    private String salaryLevel;
    private String suitableCrowd;
}
