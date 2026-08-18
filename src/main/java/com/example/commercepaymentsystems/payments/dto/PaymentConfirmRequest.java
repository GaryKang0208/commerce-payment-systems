package com.example.commercepaymentsystems.payments.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentConfirmRequest(
        @NotNull(message = "주문 ID는 필수입니다.")
        Long orderId,

        @NotNull(message = "결제 결과는 필수입니다.")
        String result,

        @NotNull(message = "결제 금액은 필수입니다.")
        Long paymentPrice

//        @NotBlank(message = "PortOne ID는 필수입니다.")
//        String portOneId
) {
}
