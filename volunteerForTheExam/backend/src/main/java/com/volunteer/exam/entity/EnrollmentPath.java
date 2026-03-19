package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("enrollment_path")
public class EnrollmentPath {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String type;
    
    private String description;
    
    private String requirements;
    
    private String universities;
    
    private String majors;
    
    private String timeline;
    
    private String advantages;
    
    private String disadvantages;
    
    private String suitableStudents;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
