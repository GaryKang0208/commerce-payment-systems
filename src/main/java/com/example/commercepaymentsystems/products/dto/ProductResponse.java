package com.example.commercepaymentsystems.products.dto;

import com.example.commercepaymentsystems.products.enums.ProductCategory;
import lombok.Getter;

import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        Integer price,
        Integer stock,
        String description,
        ProductCategory category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
