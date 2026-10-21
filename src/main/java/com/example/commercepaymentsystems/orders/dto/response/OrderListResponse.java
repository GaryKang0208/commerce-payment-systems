package com.example.commercepaymentsystems.orders.dto.response;
import java.time.LocalDateTime;

public record OrderListResponse(
        Long orderId,
        String orderNumber,
        Long totalAmount,
        String status,
        LocalDateTime createdAt
) {
}
