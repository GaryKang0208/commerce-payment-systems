package com.example.commercepaymentsystems.payments.service;

import com.example.commercepaymentsystems.orders.entity.Order;
import com.example.commercepaymentsystems.payments.dto.PaymentResponse;
import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.payments.entity.PaymentStatus;
import com.example.commercepaymentsystems.payments.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentResponse getPayment(Long customerId, Long paymentId) {
        Payment payment = paymentRepository.findByIdAndCustomerId(paymentId, customerId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        return toResponse(payment);
    }

    public Payment findByOrderIdWithOrder(Long orderId) {
        return paymentRepository.findByOrderIdWithOrder(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    @Transactional
    public void failPayment(Payment payment) {
        payment.markAsFailed();
    }

    @Transactional
    public void confirmPayment(Payment payment) {
        payment.markAsPaid();
    }

    @Transactional
    public void createPayment(Order order, Long totalPrice) {
        Payment payment = new Payment(
                totalPrice,
                PaymentStatus.IN_PROGRESS,
                order
        );

        paymentRepository.save(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getFinalPrice(),
                payment.getStatus().name(),
                payment.getPaidAt()
        );
    }
}
