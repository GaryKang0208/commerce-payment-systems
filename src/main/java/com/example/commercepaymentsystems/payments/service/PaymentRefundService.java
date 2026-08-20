package com.example.commercepaymentsystems.payments.service;


import com.example.commercepaymentsystems.payments.dto.PaymentRefundResponse;
import com.example.commercepaymentsystems.payments.entity.PaymentStatus;
import com.example.commercepaymentsystems.payments.repository.PaymentRefundRepository;
import com.example.commercepaymentsystems.cart.service.CartService;
import com.example.commercepaymentsystems.orders.entity.Order;
import com.example.commercepaymentsystems.orders.entity.OrderItem;
import com.example.commercepaymentsystems.orders.service.OrderService;
import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.products.entity.Product;
import com.example.commercepaymentsystems.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentRefundService {

    private final PaymentCommandService paymentCommandService;
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final ProductService productService;
    private final CartService cartService;
    private final PaymentRefundRepository refundRepository;

    @Transactional
    public PaymentRefundResponse cancelBeforePayment(Long paymentId) {
        Payment payment = refundRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        if (payment.getStatus() != PaymentStatus.IN_PROGRESS) { //임시 오류처리
            throw new IllegalArgumentException("결제 전 취소가 불가능한 상태입니다.");
        }
        Order order = payment.getOrder();

        payment.markAsFailed();
        orderService.cancelOrder(order);
        restoreStock(order);

        return new PaymentRefundResponse(
                payment.getId(),
                order.getId(),
                order.getStatus().name(),
                payment.getStatus().name(),
                0L,
                "결제 전 주문이 취소되었습니다."
        );
    }

    @Transactional //결제 후 취소 메서드. PaymentId를 검색하여
    public PaymentRefundResponse cancelAfterPayment(Long paymentId) {
        Payment payment = refundRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        if (payment.getStatus() != PaymentStatus.PAID) { //임시 오류처리
            throw new IllegalArgumentException("결제 후 취소가 불가능한 상태입니다.");
        }

        Order order = payment.getOrder();

        payment.markAsCancelled();
        orderService.cancelOrder(order);
        restoreStock(order);

        return new PaymentRefundResponse(
                payment.getId(),
                order.getId(),
                order.getStatus().name(),
                payment.getStatus().name(),
                payment.getFinalPrice(),
                "결제 취소 및 환불이 완료됐습니다."
        );
    }

    private void restoreStock(Order order) {
        List<OrderItem> items = orderService.getOrderItems(order.getId());

        for (OrderItem item : items) {
            Product product = productService.findEntityById(item.getProduct().getId());
            product.restoreStock(item.getQuantity());
        }
    }
}
