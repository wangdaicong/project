package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("city_employment")
public class CityEmployment {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String city;
    
    private String province;
    
    private String tier;
    
    private Integer avgSalary;
    
    private Integer jobCount;
    
    private String hotIndustries;
    
    private Integer livingCost;
    
    private String developmentPotential;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
