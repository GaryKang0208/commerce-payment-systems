package com.example.commercepaymentsystems.payments.dto;

public record PaymentCancelResponse(
        Long paymentId,
        Long orderId,
        String portoneId,
        String paymentStatus,
        String orderStatus,
        String message
) {
}
