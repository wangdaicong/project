package com.exan.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long stageId;
    private Long subjectId;

    private String type;
    private String stem;
    private Integer difficulty;
    private String analysis;
    private String answer;
    private String status;
    private Long sourceId;
    private String questionHash;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
