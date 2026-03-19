package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("major_employment")
public class MajorEmployment {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long majorId;
    
    private Integer year;
    
    private BigDecimal employmentRate;
    
    private Integer avgSalary;
    
    private Integer medianSalary;
    
    private BigDecimal matchRate;
    
    private BigDecimal upgradeRate;
    
    private String industryDistribution;
    
    private String typicalJobs;
    
    private String educationRequirement;
    
    private String dataSource;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
