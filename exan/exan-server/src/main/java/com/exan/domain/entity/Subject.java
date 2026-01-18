package com.exan.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("subject")
public class Subject {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long stageId;
    private String code;
    private String name;
    private Integer status;
    private Integer sort;
}
