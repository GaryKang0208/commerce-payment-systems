package com.example.commercepaymentsystems.payments.dto;

public record PaymentConfirmResponse(
        Long paymentId,
        Long orderId,
        Long amount,
        String paymentStatus,
        String orderStatus
) {
}
