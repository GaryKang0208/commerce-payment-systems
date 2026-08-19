package com.example.commercepaymentsystems.cart.dto.response;

public record CartItemResponse(
        Long cartItemId,
        Long productId,
        String productName,
        int price,
        int quantity,
        Long itemTotalPrice
) {}
