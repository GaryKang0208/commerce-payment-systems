package com.example.commercepaymentsystems.payments.port;

public interface PaymentGateway {
    PaymentGatewayResponse getPayment(String portoneId);
    void cancelPayment(String portoneId, Long amount, String reason);
}
