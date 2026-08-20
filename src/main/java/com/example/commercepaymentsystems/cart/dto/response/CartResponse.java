package com.example.commercepaymentsystems.cart.dto.response;

import java.util.List;

public record CartResponse(
List<CartItemResponse> items,
    int totalQuantity,
    Long totalPrice
){}
