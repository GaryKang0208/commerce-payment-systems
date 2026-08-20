package com.example.commercepaymentsystems.products.dto;

import java.util.List;

public record ProductPageResponse (
        List<ProductResponse> products,
        long totalCount,
        int page,
        int size
){
}
