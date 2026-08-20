package com.example.commercepaymentsystems.orders.dto.response;

// 주문 생성
public record CreateOrderResponse(
        Long orderId, String orderNumber, Long totalAmount, String status) {
}
