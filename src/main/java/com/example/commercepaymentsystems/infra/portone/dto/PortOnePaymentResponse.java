package com.example.commercepaymentsystems.infra.portone.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOnePaymentResponse(
        String id,
        String status,
        PaymentAmount amount
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaymentAmount(
            int total
    ) {
    }
}
