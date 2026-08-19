package com.example.commercepaymentsystems.cart.dto.response;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        int price,
        int quantity,
        int stock
) {}
