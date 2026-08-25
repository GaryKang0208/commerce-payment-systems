package com.example.commercepaymentsystems.orders.dto.response;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long orderId,
        String orderNumber,
        Long totalAmount,
        Long pointUsed,
        String status,
        LocalDateTime createdAt,
        List<OrderItemResponse> orderItems
) {
    public record OrderItemResponse(
            String productName,
            Long productPrice,
            Integer quantity
    ) {
    }
}
