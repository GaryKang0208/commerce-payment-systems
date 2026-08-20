package com.example.commercepaymentsystems.payments.service;

import com.example.commercepaymentsystems.cart.entity.CartItemEntity;
import com.example.commercepaymentsystems.cart.service.CartService;
import com.example.commercepaymentsystems.order.entity.Order;
import com.example.commercepaymentsystems.order.entity.OrderItem;
import com.example.commercepaymentsystems.order.service.OrderService;
import com.example.commercepaymentsystems.payments.dto.PaymentConfirmResponse;
import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.products.entity.Product;
import com.example.commercepaymentsystems.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        cartService.removeAllItems(order.getCustomer().getId());

        return new PaymentConfirmResponse(
                payment.getId(),
                orderId,
                payment.getFinalPrice(),
                payment.getStatus().name(),
                order.getOrderStatus().name()
        );
    }

    private void restoreStock(Order order) {
        List<OrderItem> items = orderService.getOrderItems(order.getId());

        for (OrderItem item : items) {
            Product product = productService.findProductById(item.getProduct().getId());
            product.restoreStock(item.getQuantity());
        }
    }
}
