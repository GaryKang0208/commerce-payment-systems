package com.example.commercepaymentsystems.customers.dto.customers;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,
        @NotBlank(message = "이름은 필수입니다.")
        String name,
        @NotBlank(message = "전화번호는 필수입니다.")
        String phoneNumber) {
}
