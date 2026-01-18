package com.exan.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Orders {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long productId;
    private String orderNo;
    private Integer amountCent;
    private String status;
    private String payChannel;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}
