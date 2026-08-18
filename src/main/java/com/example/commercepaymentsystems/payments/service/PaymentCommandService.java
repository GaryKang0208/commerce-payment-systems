package com.example.commercepaymentsystems.payments.service;

import com.example.commercepaymentsystems.cart.service.CartService;
import com.example.commercepaymentsystems.orders.entity.Order;
import com.example.commercepaymentsystems.orders.entity.OrderItem;
import com.example.commercepaymentsystems.orders.service.OrderService;
import com.example.commercepaymentsystems.payments.dto.PaymentConfirmResponse;
import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.products.entity.Product;
import com.example.commercepaymentsystems.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentCommandService {
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final ProductService productService;
    private final CartService cartService;

    @Transactional
    public void failPaymentAndOrder(Long orderId) {
        Payment payment = paymentService.findByOrderIdWithOrder(orderId);
        Order order = payment.getOrder();

        paymentService.failPayment(payment);
        orderService.cancelOrder(order);

        restoreStock(order);
    }

    @Transactional
    public PaymentConfirmResponse approvePaymentAndOrder(Long orderId) {
        Payment payment = paymentService.findByOrderIdWithOrder(orderId);
        Order order = payment.getOrder();

        paymentService.confirmPayment(payment);
        orderService.confirmOrder(order);

        cartService.clearCartItems();

        return new PaymentConfirmResponse(
                payment.getId(),
                orderId,
                payment.getFinalPrice(),
                payment.getStatus().name(),
                order.getStatus().name()
        );
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = productService.findProductEntity(item.getProduct().getId());
            product.restoreStock(item.getQuantity());
        }
    }
}
