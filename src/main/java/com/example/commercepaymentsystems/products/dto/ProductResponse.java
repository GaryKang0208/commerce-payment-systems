package com.example.commercepaymentsystems.products.dto;

import com.example.commercepaymentsystems.products.enums.ProductCategory;
import com.example.commercepaymentsystems.products.enums.ProductStatus;
import lombok.Getter;

import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        Long price,
        Integer stock,
        String description,
        ProductCategory category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        ProductStatus salesStatus
) {
}
