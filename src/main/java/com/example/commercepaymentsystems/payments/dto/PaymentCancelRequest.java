package com.example.commercepaymentsystems.payments.dto;
import jakarta.validation.constraints.Size;

public record PaymentCancelRequest(
        @Size(max = 200, message = "취소 사유는 200자 이내여야 합니다.")
        String reason
) {
}
