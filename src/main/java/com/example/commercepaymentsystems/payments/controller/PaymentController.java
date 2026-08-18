package com.example.commercepaymentsystems.payments.controller;

import com.example.commercepaymentsystems.payments.dto.PaymentResponse;
import com.example.commercepaymentsystems.payments.facade.PaymentFacade;
import com.example.commercepaymentsystems.payments.service.PaymentCommandService;
import com.example.commercepaymentsystems.payments.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentCommandService paymentCommandService;
    private final PaymentFacade paymentFacade;

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @AuthenticationPrincipal Long customerId,
            @PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPayment(customerId, id));
    }
}
