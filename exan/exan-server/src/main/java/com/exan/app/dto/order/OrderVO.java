package com.exan.app.dto.order;

import java.time.LocalDateTime;

public record OrderVO(
    String orderNo,
    Integer amountCent,
    String status,
    LocalDateTime createdAt,
    LocalDateTime paidAt
) {
}
