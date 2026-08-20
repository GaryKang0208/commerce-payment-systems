package com.example.commercepaymentsystems.orders.dto.request;

import java.util.List;

// 주문 생성
public record CreateOrderRequest(List<Long> cartItemIds) {

    // null이 들어오면 빈 리스트로 변화
    // 빈 리스트는 전체 장비구니 주문으로 처리
    public CreateOrderRequest {
        if (cartItemIds == null) {
            cartItemIds = List.of();
        }
    }
}
