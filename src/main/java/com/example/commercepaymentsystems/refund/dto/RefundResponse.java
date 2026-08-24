package com.example.commercepaymentsystems.refund.dto;

public record RefundResponse(
        Long paymentId,
        Long orderId,
        String orderStatus,
        String paymentStatus,
        Long refundAmount,
        String refundStatus,
        String message
) { }
