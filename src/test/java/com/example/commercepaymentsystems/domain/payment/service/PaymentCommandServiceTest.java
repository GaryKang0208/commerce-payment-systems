package com.example.commercepaymentsystems.domain.payment.service;

import com.example.commercepaymentsystems.cart.service.CartService;
import com.example.commercepaymentsystems.common.exception.BusinessException;
import com.example.commercepaymentsystems.common.exception.ErrorCode;
import com.example.commercepaymentsystems.customers.entity.Customers;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
                new Customers(
                        "email",
                        "password",
                        "name",
                        "000-0000-0000"
                ),
                "order_num",
                10000L
        );
        Payment payment = new Payment(
                10000L,
                PaymentStatus.IN_PROGRESS,
                order,
                0L
        );

        given(paymentService.findByOrderIdWithOrder(anyLong())).willReturn(payment);
        willAnswer(i -> {
            payment.markAsFailed();
            return null;
        }).given(paymentService).failPayment(payment);
        willAnswer(invocation -> {
            order.cancel();
            return null;
        }).given(orderService).cancelOrder(order);

        //when
        paymentCommandService.failPaymentAndOrder(1L);

        //then
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals(OrderStatus.CANCELED, payment.getOrder().getStatus());
    }

    @Test
    @DisplayName("결제 실패 로직 테스트 - 실패(상태 변경 불가)")
    void failPaymentAndOrder_test_failure_invalid_status() {
        //given
        Order order = new Order(
                new Customers(
                        "email",
                        "password",
                        "name",
                        "000-0000-0000"
                ),
                "ord_num",
                10000L
        );
        Payment payment = new Payment(
                10000L,
                PaymentStatus.CANCELLED,
                order,
                0L
        );

        given(paymentService.findByOrderIdWithOrder(anyLong())).willReturn(payment);
        willAnswer(i -> {
            payment.markAsFailed();
            return null;
        }).given(paymentService).failPayment(payment);

        //when&hen
        assertThatThrownBy(() -> paymentCommandService.failPaymentAndOrder(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_PAYMENT_STATUS.getMessage());
    }

    @Test
    @DisplayName("결제 승인 로직 테스트 - 성공")
    void approvePaymentAndOrder_success() {
        //given
        Order order = new Order(
                new Customers(
                        "email",
                        "password",
                        "name",
                        "000-0000-0000"
                ),"ord_num",
                10000L
        );
        Payment payment = new Payment(
                10000L,
                PaymentStatus.IN_PROGRESS,
                order,
                0L
        );

        given(paymentService.findByOrderIdWithOrder(anyLong())).willReturn(payment);
        willAnswer(i -> {
            payment.markAsPaid();
            return null;
        }).given(paymentService).confirmPayment(payment);
        willAnswer(invocation -> {
            order.confirm();
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
                new Customers(
                        "email",
                        "password",
                        "name",
                        "000-0000-0000"
                ),
                "ord_num",
                10000L
        );
        order.cancel();
        Payment payment = new Payment(
                10000L,
                PaymentStatus.IN_PROGRESS,
                order,
                0L
        );

        given(paymentService.findByOrderIdWithOrder(anyLong())).willReturn(payment);
        willAnswer(i -> {
            payment.markAsPaid();
            return null;
        }).given(paymentService).confirmPayment(payment);
        willAnswer(invocation -> {
            order.confirm();
            return null;
        }).given(orderService).confirmOrder(order);

        //when&then
        assertThatThrownBy(() -> paymentCommandService.approvePaymentAndOrder(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_ORDER_STATUS.getMessage());
    }
}