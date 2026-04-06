package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("university_department")
public class UniversityDepartment implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("university_id")
    private Long universityId;
    
    private String name;
    
    @TableField("major_count")
    private Integer majorCount;
    
    private String introduction;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
}
