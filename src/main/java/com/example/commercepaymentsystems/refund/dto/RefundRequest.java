package com.example.commercepaymentsystems.refund.dto;

import jakarta.validation.constraints.Size;
import java.util.List;

public record RefundRequest(
        @Size(max = 200, message = "취소 사유는 200자 이내여야 합니다") String reason,
        List<RefundItemRequest> items
) {
}