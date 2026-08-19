package com.example.commercepaymentsystems.customers.dto.auth;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupRequest(
        @Email
        @NotBlank
        String email,
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,16}$",
                message = "비밀번호는 8~16자 영문, 숫자, 특수문자를 최소 1개씩 포함해야 합니다.")
        @NotBlank String password,
        @NotBlank  String name,
        @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호는 010-XXXX-XXXX 형식이어야 합니다.")
        @NotBlank String phoneNumber
) {}

