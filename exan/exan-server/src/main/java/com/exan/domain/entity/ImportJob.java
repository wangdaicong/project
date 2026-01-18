package com.exan.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("import_job")
public class ImportJob {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String jobType;
    private Long stageId;
    private Long subjectId;
    private String status;

    private Integer totalCount;
    private Integer insertedCount;
    private Integer duplicateCount;
    private Integer failedCount;

    private String originalFilename;
    private String storedFilePath;

    private LocalDateTime createdAt;
}
