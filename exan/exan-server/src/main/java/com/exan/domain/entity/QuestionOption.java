package com.exan.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("question_option")
public class QuestionOption {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionId;
    private String optKey;
    private String content;
}
