package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("career")
public class Career {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private Long industryId;
    
    private Integer avgSalary;
    
    private String salaryRange;
    
    private String educationRequirement;
    
    private String skillRequirements;
    
    private String careerPath;
    
    private String description;
    
    private Integer jobCount;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
