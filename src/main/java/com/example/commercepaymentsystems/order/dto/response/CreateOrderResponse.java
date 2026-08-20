package com.example.commercepaymentsystems.order.dto.response;

// 주문 생성
public record CreateOrderResponse(
        Long orderId, String orderNumber, Long totalAmount, String status) {
}
