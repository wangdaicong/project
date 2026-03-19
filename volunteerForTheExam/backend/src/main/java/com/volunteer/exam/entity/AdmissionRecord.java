package com.volunteer.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("admission_record")
public class AdmissionRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long universityId;
    private Long majorId;
    private String province;
    private Integer year;
    private Integer minScore;
    private Integer avgScore;
    private Integer maxScore;
    private Integer enrollmentNumber;
    private String batch;
}
