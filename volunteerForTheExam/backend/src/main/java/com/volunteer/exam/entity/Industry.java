package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("industry")
public class Industry {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String category;
    
    private Integer avgSalary;
    
    private BigDecimal growthRate;
    
    private Integer jobCount;
    
    private String description;
    
    private String trend;
    
    private String hotCities;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
