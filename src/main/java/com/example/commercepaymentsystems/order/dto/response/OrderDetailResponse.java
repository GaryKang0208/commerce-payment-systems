package com.example.commercepaymentsystems.order.dto.response;

import java.time.LocalDateTime;
import java.util.List;

// 내 주문 상세 조회
public record OrderDetailResponse(
        Long orderId,
        String orderNumber,
        Long totalAmount,
        String status,
        LocalDateTime createdAt,
        List<OrderItemResponse> orderItems) {

    public record OrderItemResponse(
            String productName,
            Long productPrice,
            Integer quantity
    ) {
    }
}
