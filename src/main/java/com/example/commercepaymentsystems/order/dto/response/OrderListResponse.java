package com.example.commercepaymentsystems.order.dto.response;

import java.time.LocalDateTime;

// 내 주문 목록 조회
public record OrderListResponse(
        Long orderId, String orderNumber, Long totalAmount, String status, LocalDateTime createdAt) {
}
