package com.example.commercepaymentsystems.refund.dto;

import jakarta.validation.constraints.NotNull;

public record RefundItemRequest(

        @NotNull(message = "주문 상품 ID는 필수입니다")
        Long orderItemId,
        @NotNull(message = "환불 수량은 필수입니다")
        Integer quantity

) {
}