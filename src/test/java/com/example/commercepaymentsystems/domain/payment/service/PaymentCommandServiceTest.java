package com.example.commercepaymentsystems.domain.payment.service;

import com.example.commercepaymentsystems.cart.service.CartService;
import com.example.commercepaymentsystems.customers.entity.Customer;
import com.example.commercepaymentsystems.orders.entity.Order;
import com.example.commercepaymentsystems.orders.entity.OrderStatus;
import com.example.commercepaymentsystems.orders.service.OrderService;
import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.payments.entity.PaymentStatus;
import com.example.commercepaymentsystems.payments.service.PaymentCommandService;
import com.example.commercepaymentsystems.payments.service.PaymentService;
import com.example.commercepaymentsystems.products.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;

@ExtendWith(MockitoExtension.class)
class PaymentCommandServiceTest {
    @Mock
    PaymentService paymentService;
    @Mock
    OrderService orderService;
    @Mock
    CartService cartService;
    @Mock
    ProductService productService;
    @InjectMocks
    private PaymentCommandService paymentCommandService;

    @Test
    @DisplayName("결제 실패 로직 테스트 - 성공")
    void failPaymentAndOrder_test_success() {
        //given
        Order order = new Order(
                new Customer(
                        "email",
                        "password",
                        "name"
                ),
                OrderStatus.PENDING_PAYMENT,
                10000L,
                List.of()
        );
        Payment payment = new Payment(
                10000L,
                PaymentStatus.IN_PROGRESS,
                order
        );

        given(paymentService.findByOrderIdWithOrder(anyLong())).willReturn(payment);
        willAnswer(i -> {
            payment.markAsFailed();
            return null;
        }).given(paymentService).failPayment(payment);
        willAnswer(invocation -> {
            order.markAsCancelled();
            return null;
        }).given(orderService).cancelOrder(order);

        //when
        paymentCommandService.failPaymentAndOrder(1L);

        //then
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals(OrderStatus.CANCELLED, payment.getOrder().getStatus());
    }

    @Test
    @DisplayName("결제 실패 로직 테스트 - 실패(상태 변경 불가)")
    void failPaymentAndOrder_test_failure_invalid_status() {
        //given
        Order order = new Order(
                new Customer(
                        "email",
                        "password",
                        "name"
                ),
                OrderStatus.PENDING_PAYMENT,
                10000L,
                List.of()
        );
        Payment payment = new Payment(
                10000L,
                PaymentStatus.CANCELLED,
                order
        );

        given(paymentService.findByOrderIdWithOrder(anyLong())).willReturn(payment);
        willAnswer(i -> {
            payment.markAsFailed();
            return null;
        }).given(paymentService).failPayment(payment);

        //when&hen
        assertThrows(RuntimeException.class, () -> paymentCommandService.failPaymentAndOrder(1L));
    }

    @Test
    @DisplayName("결제 승인 로직 테스트 - 성공")
    void approvePaymentAndOrder_success() {
        //given
        Order order = new Order(
                new Customer(
                        "email",
                        "password",
                        "name"
                ),
                OrderStatus.PENDING_PAYMENT,
                10000L,
                List.of()
        );
        Payment payment = new Payment(
                10000L,
                PaymentStatus.IN_PROGRESS,
                order
        );

        given(paymentService.findByOrderIdWithOrder(anyLong())).willReturn(payment);
        willAnswer(i -> {
            payment.markAsPaid();
            return null;
        }).given(paymentService).confirmPayment(payment);
        willAnswer(invocation -> {
            order.markAsConfirmed();
            return null;
        }).given(orderService).confirmOrder(order);

        //when
        paymentCommandService.approvePaymentAndOrder(1L);

        //then
        assertEquals(PaymentStatus.PAID, payment.getStatus());
        assertEquals(OrderStatus.CONFIRMED, payment.getOrder().getStatus());
    }

    @Test
    @DisplayName("결제 승인 로직 테스트 - 실패(상태 변경 불가)")
    void approvePaymentAndOrder_failure_invalid_status() {
        //given
        Order order = new Order(
                new Customer(
                        "email",
                        "password",
                        "name"
                ),
                OrderStatus.CANCELLED,
                10000L,
                List.of()
        );
        Payment payment = new Payment(
                10000L,
                PaymentStatus.IN_PROGRESS,
                order
        );

        given(paymentService.findByOrderIdWithOrder(anyLong())).willReturn(payment);
        willAnswer(i -> {
            payment.markAsPaid();
            return null;
        }).given(paymentService).confirmPayment(payment);
        willAnswer(invocation -> {
            order.markAsConfirmed();
            return null;
        }).given(orderService).confirmOrder(order);

        //when&then
        assertThrows(RuntimeException.class, () -> paymentCommandService.approvePaymentAndOrder(1L));
    }
}