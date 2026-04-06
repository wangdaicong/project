package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("enrollment_brochure")
public class EnrollmentBrochure implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("university_id")
    private Long universityId;
    
    private String title;
    
    private Integer year;
    
    private String type;
    
    private String content;
    
    @TableField("file_url")
    private String fileUrl;
    
    @TableField("publish_date")
    private LocalDate publishDate;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("update_time")
    private LocalDateTime updateTime;
}
