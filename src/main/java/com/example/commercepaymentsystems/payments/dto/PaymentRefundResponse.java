package com.example.commercepaymentsystems.payments.dto;

public record PaymentRefundResponse(
        Long paymentId,
        Long orderId,
        String orderStatus,
        String paymentStatus,
        Long refundAmount,
        String message
) {
}
