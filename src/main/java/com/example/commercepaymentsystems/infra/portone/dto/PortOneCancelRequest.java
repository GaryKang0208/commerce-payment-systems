package com.example.commercepaymentsystems.infra.portone.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PortOneCancelRequest (
        String reason,
        Long amount,
        String storeId
) {
}
