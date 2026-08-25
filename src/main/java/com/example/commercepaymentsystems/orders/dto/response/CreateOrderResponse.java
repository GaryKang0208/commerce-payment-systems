package com.example.commercepaymentsystems.orders.dto.response;

public record CreateOrderResponse(
        Long orderId,
        String orderNumber,
        Long totalAmount,
        Long pointUsed,
        String status
) {
}
