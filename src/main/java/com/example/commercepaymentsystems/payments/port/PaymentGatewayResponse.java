package com.example.commercepaymentsystems.payments.port;

public record PaymentGatewayResponse(
        String id,
        String status,
        long totalAmount
) {
}
