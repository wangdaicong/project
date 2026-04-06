package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 志愿填报记录实体类
 */
@Data
@TableName("volunteer_application")
public class VolunteerApplication {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String studentName;
    
    private String province;
    
    private Integer score;
    
    private String category;
    
    private Integer rankPosition;
    
    private String batch;
    
    private String status;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;
}
