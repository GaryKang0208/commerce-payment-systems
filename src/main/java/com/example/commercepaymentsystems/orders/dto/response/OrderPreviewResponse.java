package com.example.commercepaymentsystems.orders.dto.response;
import java.util.List;

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
