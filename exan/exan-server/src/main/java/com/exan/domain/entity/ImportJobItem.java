package com.exan.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("import_job_item")
public class ImportJobItem {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long jobId;
    private Long questionId;
    private Long subjectId;
    private String questionHash;
    private String result;
    private String message;

    private LocalDateTime createdAt;
}
