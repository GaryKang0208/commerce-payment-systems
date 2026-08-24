package com.example.commercepaymentsystems.refund.facade;

import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.payments.port.PaymentGateway;
import com.example.commercepaymentsystems.payments.service.PaymentService;
import com.example.commercepaymentsystems.refund.dto.RefundRequest;
import com.example.commercepaymentsystems.refund.dto.RefundResponse;
import com.example.commercepaymentsystems.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefundFacade {
    private final PaymentService paymentService;
    private final RefundService refundService;
    private final PaymentGateway paymentGateway;

    public RefundResponse refund(Long paymentId, Long customerId, RefundRequest refundRequest) {
        RefundResponse response = refundService.refund(paymentId, customerId, refundRequest);
        Payment payment = paymentService.findByIdWithOrder(paymentId);

        paymentGateway.cancelPayment(payment.getPortoneId(), response.refundAmount(), refundRequest.reason());

        return response;
    }
}
