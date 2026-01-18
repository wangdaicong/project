package com.exan.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exam_answer")
public class ExamAnswer {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;
    private Long questionId;

    private String answerJson;
    private Integer isCorrect;
    private Integer scoreGot;
    private LocalDateTime answeredAt;
}
