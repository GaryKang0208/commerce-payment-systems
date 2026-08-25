package com.example.commercepaymentsystems.orders.dto.request;
import java.util.List;

public record OrderPreviewRequest(
        List<Long> cartItemIds
) {
    public OrderPreviewRequest {
        if (cartItemIds == null) {
            cartItemIds = List.of();
        }
    }
}
