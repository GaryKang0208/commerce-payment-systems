package com.example.commercepaymentsystems.customers.dto.customers;

public record ProfileResponse(
        Long id,
        String email,
        String name,
        String phoneNumber,
        Long point
) {

}
