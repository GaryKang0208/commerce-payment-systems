package com.example.commercepaymentsystems.payments.dto;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long totalPrice,
        String status,
        LocalDateTime paidAt
) {
}
