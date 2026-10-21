package com.example.commercepaymentsystems.point.dto;
import com.example.commercepaymentsystems.point.entity.Point;

import java.time.LocalDateTime;

public record PointTransactionResponse(
        Long id,
        Long customerId,
        Long paymentId,
        String type,
        Long amount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PointTransactionResponse from(Point pt) {
        return new PointTransactionResponse(
                pt.getId(),
                pt.getCustomers().getId(),
                pt.getPayment() != null ? pt.getPayment().getId() : null,
                pt.getType().name(),
                pt.getAmount(),
                pt.getCreatedAt(),
                pt.getUpdatedAt()
        );
    }
}
