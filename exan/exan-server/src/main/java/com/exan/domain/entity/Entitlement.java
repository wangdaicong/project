package com.exan.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("entitlement")
public class Entitlement {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String entType;
    private Long refId;
    private Long orderId;
    private String status;
    private LocalDateTime createdAt;
}
