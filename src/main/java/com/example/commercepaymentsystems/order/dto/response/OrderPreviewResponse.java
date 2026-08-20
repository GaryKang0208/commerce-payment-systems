package com.example.commercepaymentsystems.order.dto.response;

import java.util.List;

// 주문 미리보기
public record OrderPreviewResponse(
        List<OrderPreviewItemResponse> items,
        Long totalAmount
) {
    public record OrderPreviewItemResponse(
            Long productId,
            String productName,
            Long price,
            Integer quantity,
            Long subtotal
    ) {
    }
}
