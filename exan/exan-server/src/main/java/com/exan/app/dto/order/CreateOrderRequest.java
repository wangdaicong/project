package com.exan.app.dto.order;

import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
    @NotNull Long productId
) {
}
