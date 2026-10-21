package com.example.commercepaymentsystems.orders.dto.request;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record CreateOrderRequest(
        List<Long> cartItemIds,
        @PositiveOrZero(message = "사용 포인트는 0 이상이어야 합니다.")
        Long pointUsed
) {
    public CreateOrderRequest {
        if (cartItemIds == null) {
            cartItemIds = List.of();
        }
        if (pointUsed == null) {
            pointUsed = 0L;
        }
    }
}
