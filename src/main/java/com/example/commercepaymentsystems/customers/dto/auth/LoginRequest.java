package com.example.commercepaymentsystems.customers.dto.auth;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email
        @NotBlank
        String email,
        @NotBlank
        String password
) {
}
