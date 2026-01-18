package com.exan.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("exam_session_question")
public class ExamSessionQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;
    private Long questionId;
    private Integer score;
    private Integer sort;
}
