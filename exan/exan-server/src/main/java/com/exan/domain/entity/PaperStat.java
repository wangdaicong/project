package com.exan.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("paper_stat")
public class PaperStat {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long paperId;

    private Long views;

    private Long downloads;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
