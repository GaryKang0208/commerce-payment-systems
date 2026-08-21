package com.example.commercepaymentsystems.payments.controller;

import com.example.commercepaymentsystems.payments.dto.PaymentRefundRequest;
import com.example.commercepaymentsystems.payments.dto.PaymentRefundResponse;
import com.example.commercepaymentsystems.payments.facade.PaymentFacade;
import com.example.commercepaymentsystems.payments.service.PaymentCommandService;
import com.example.commercepaymentsystems.payments.service.PaymentRefundService;
import com.example.commercepaymentsystems.payments.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class RefundController {

    private final PaymentService paymentService;
    private final PaymentCommandService paymentCommandService;
    private final PaymentFacade paymentFacade;
    private final PaymentRefundService paymentRefundService;

    @PostMapping("/{id}/cancel-before-payment")
    public ResponseEntity<PaymentRefundResponse> cancelBeforePayment(
            @PathVariable("id") Long paymentId,
            @RequestBody PaymentRefundRequest request
    ) {
        return ResponseEntity.ok(paymentRefundService.cancelBeforePayment(paymentId));
    }

    @PostMapping("/{id}/cancel-after-payment")
    public ResponseEntity<PaymentRefundResponse> cancelAfterPayment(
            @PathVariable("id") Long paymentId,
            @RequestBody PaymentRefundRequest request
    ) {
        return ResponseEntity.ok(paymentRefundService.cancelAfterPayment(paymentId));
    }
}
