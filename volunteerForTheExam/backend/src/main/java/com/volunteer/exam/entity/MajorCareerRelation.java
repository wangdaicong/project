package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("major_career_relation")
public class MajorCareerRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long majorId;
    
    private Long careerId;
    
    private Integer matchDegree;
    
    private BigDecimal employmentPercentage;
    
    private LocalDateTime createdAt;
}
