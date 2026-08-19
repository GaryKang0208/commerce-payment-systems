package com.example.commercepaymentsystems.cart.dto.response;

public record AddCartResponse(
        Long productId,
        String productName,
        int quantity,
        int price,
        int totalAmount,
        Long cartItemId
) {}
