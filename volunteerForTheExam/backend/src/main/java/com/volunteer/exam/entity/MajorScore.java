package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("major_score")
public class MajorScore {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long majorId;
    
    private Integer employmentScore;
    
    private Integer salaryScore;
    
    private Integer developmentScore;
    
    private Integer stabilityScore;
    
    private Integer totalScore;
    
    private String recommendationLevel;
    
    private Integer year;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
