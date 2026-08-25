package com.example.commercepaymentsystems.refund.controller;

import com.example.commercepaymentsystems.common.ApiResponse;
import com.example.commercepaymentsystems.refund.dto.RefundRequest;
import com.example.commercepaymentsystems.refund.dto.RefundResponse;
import com.example.commercepaymentsystems.refund.facade.RefundFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class RefundController {
    private final RefundFacade refundFacade;

    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<RefundResponse>> refund(
            @AuthenticationPrincipal Long customerId,
            @PathVariable("id") Long paymentId,
            @RequestBody RefundRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(refundFacade.refund(paymentId, customerId, request)));
    }
}
