package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("vocational_college_info")
public class VocationalCollegeInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long universityId;
    
    private Boolean isDoubleHigh;
    
    private Boolean isDemonstration;
    
    private String level;
    
    private String featuredMajors;
    
    private String enterpriseCooperation;
    
    private String orderTraining;
    
    private BigDecimal upgradeRate;
    
    private BigDecimal employmentRate;
    
    private String internshipOpportunities;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
