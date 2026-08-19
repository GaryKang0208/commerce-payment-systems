package com.example.commercepaymentsystems.cart.dto.request;

import jakarta.validation.constraints.Min;

public record UpdateCartRequest (
    @Min(value = 1, message = "수량은 1 이상이어야 합니다.") int quantity)
{}
