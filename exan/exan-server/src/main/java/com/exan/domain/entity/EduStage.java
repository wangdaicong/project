package com.exan.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("edu_stage")
public class EduStage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;
    private String name;
    private Integer status;
    private Integer sort;
}
