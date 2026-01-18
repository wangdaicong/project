package com.exan.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("paper")
public class Paper {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long stageId;
    private Long subjectId;

    private String name;
    private String paperType;

    private Integer totalScore;
    private Integer timeLimitSec;

    private Integer version;
    private String status;

    private String pricingType;
    private Integer priceCent;

    private LocalDate paperDate;
    private String regionCode;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
