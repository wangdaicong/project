package com.exan.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exam_session")
public class ExamSession {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String mode;
    private Long stageId;
    private Long subjectId;
    private Long paperId;
    private String status;

    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;

    private Integer timeLimitSec;
    private Integer scoreTotal;
    private Integer scoreGot;

    private LocalDateTime createdAt;
}
