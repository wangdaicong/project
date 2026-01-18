package com.exan.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("paper_content")
public class PaperContent {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long paperId;

    private String sourceUrl;

    private String contentText;

    private String attachmentsJson;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
