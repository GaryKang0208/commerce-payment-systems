package com.example.commercepaymentsystems.domain.payment.facade;

import com.example.commercepaymentsystems.common.exception.BusinessException;
import com.example.commercepaymentsystems.common.exception.ErrorCode;
import com.example.commercepaymentsystems.customers.entity.Customers;
import com.example.commercepaymentsystems.orders.entity.Order;
import com.example.commercepaymentsystems.orders.entity.OrderStatus;
import com.example.commercepaymentsystems.payments.dto.PaymentConfirmRequest;
import com.example.commercepaymentsystems.payments.dto.PaymentConfirmResponse;
import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.payments.entity.PaymentStatus;
import com.example.commercepaymentsystems.payments.facade.PaymentFacade;
import com.example.commercepaymentsystems.payments.service.PaymentCommandService;
import com.example.commercepaymentsystems.payments.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentFacadeTest {
    @Mock
    private PaymentService paymentService;
    @Mock
    private PaymentCommandService paymentCommandService;
    @InjectMocks
    private PaymentFacade paymentFacade;

    @Test
    @DisplayName("결제 승인 테스트 - 성공")
    void paymentConfirm_success() {
        //given
        Customers customer = new Customers(
                "email",
                "password",
                "name",
                "000-0000-0000"
        );
        ReflectionTestUtils.setField(customer, "id", 1L);

        Order order = new Order(
                customer,
                "ordernum",
                10000L
        );
        ReflectionTestUtils.setField(order, "id", 1L);

        Payment payment = new Payment(
                10000L,
                PaymentStatus.IN_PROGRESS,
                order
        );
        ReflectionTestUtils.setField(payment, "id", 1L);

        PaymentConfirmRequest request = new PaymentConfirmRequest(
                order.getId(),
                "SUCCESS",
                10000L
        );

        given(paymentService.findByOrderIdWithOrder(anyLong())).willReturn(payment);
        given(paymentCommandService.approvePaymentAndOrder(anyLong()))
                .willReturn(new PaymentConfirmResponse(
                        1L,
                        1L,
                        10000L,
                        payment.getStatus().name(),
                        order.getStatus().name()
                ));

        //when
        PaymentConfirmResponse res = paymentFacade.paymentConfirm(1L, request);

        //then
        assertEquals(10000L, res.amount());
    }

    @Test
    @DisplayName("결제 승인 테스트 - 실패")
    void paymentConfirm_purchase_fail_success() {
        //given
        Customers customer = new Customers(
                "email",
                "password",
                "name",
                "000-0000-0000"
        );
        ReflectionTestUtils.setField(customer, "id", 1L);

        Order order = new Order(
                customer,
                "ordernum",
                10000L
        );
        ReflectionTestUtils.setField(order, "id", 1L);

        Payment payment = new Payment(
                10000L,
                PaymentStatus.IN_PROGRESS,
                order
        );
        ReflectionTestUtils.setField(payment, "id", 1L);

        PaymentConfirmRequest request = new PaymentConfirmRequest(
                order.getId(),
                "FAIL",
                10000L
        );

        given(paymentService.findByOrderIdWithOrder(anyLong())).willReturn(payment);

        //when&then
        assertThatThrownBy(() -> paymentFacade.paymentConfirm(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.PG_FAILURE.getMessage());
        verify(paymentCommandService).failPaymentAndOrder(anyLong());

    }

    @Test
    @DisplayName("결제 승인 테스트 - 실패(결제 상태가 IN_PROGRESS가 아님)")
    void paymentConfirm_failure_paymentStatus_not_IN_PROGRESS() {
        //given
        Customers customer = new Customers(
                "email",
                "password",
                "name",
                "000-0000-0000"
        );
        ReflectionTestUtils.setField(customer, "id", 1L);

        Order order = new Order(
                customer,
                "ordernum",
                10000L
        );
        ReflectionTestUtils.setField(order, "id", 1L);

        Payment payment = new Payment(
                10000L,
                PaymentStatus.FAILED,
                order
        );
        ReflectionTestUtils.setField(payment, "id", 1L);

        PaymentConfirmRequest request = new PaymentConfirmRequest(
                order.getId(),
                "SUCCESS",
                10000L
        );

        given(paymentService.findByOrderIdWithOrder(anyLong())).willReturn(payment);

        //when&then
        assertThatThrownBy(() -> paymentFacade.paymentConfirm(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ALREADY_PROCESSED_PAYMENT.getMessage());
    }

    @Test
    @DisplayName("결제 승인 테스트 - 실패(주문자가 아님)")
    void paymentConfirm_failure_not_order_owner() {
        //given
        Customers customer = new Customers(
                "email",
                "password",
                "name",
                "000-0000-0000"
        );
        ReflectionTestUtils.setField(customer, "id", 1L);

        Order order = new Order(
                customer,
                "ordernum",
                10000L
        );
        ReflectionTestUtils.setField(order, "id", 1L);

        Payment payment = new Payment(
                10000L,
                PaymentStatus.IN_PROGRESS,
                order
        );
        ReflectionTestUtils.setField(payment, "id", 1L);

        PaymentConfirmRequest request = new PaymentConfirmRequest(
                order.getId(),
                "SUCCESS",
                10000L
        );

        given(paymentService.findByOrderIdWithOrder(anyLong())).willReturn(payment);

        //when&then
        assertThatThrownBy(() -> paymentFacade.paymentConfirm(2L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("결제 승인 테스트 - 실패(주문 상태가 유효하지 않음)")
    void paymentConfirm_failure_invalid_order_status() {
        //given
        Customers customer = new Customers(
                "email",
                "password",
                "name",
                "000-0000-0000"
        );
        ReflectionTestUtils.setField(customer, "id", 1L);

        Order order = new Order(
                customer,
                "ordernum",
                10000L
        );
        ReflectionTestUtils.setField(order, "id", 1L);
        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.CANCELED);

        Payment payment = new Payment(
                10000L,
                PaymentStatus.IN_PROGRESS,
                order
        );
        ReflectionTestUtils.setField(payment, "id", 1L);

        PaymentConfirmRequest request = new PaymentConfirmRequest(
                order.getId(),
                "SUCCESS",
                10000L
        );

        given(paymentService.findByOrderIdWithOrder(anyLong())).willReturn(payment);

        //when&then
        assertThatThrownBy(() -> paymentFacade.paymentConfirm(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_ORDER_STATUS.getMessage());
    }

    @Test
    @DisplayName("결제 승인 테스트 - 실패(결제 금액이 다름)")
    void paymentConfirm_failure_purchase_amount_not_equal() {
        //given
        Customers customer = new Customers(
                "email",
                "password",
                "name",
                "000-0000-0000"
        );
        ReflectionTestUtils.setField(customer, "id", 1L);

        Order order = new Order(
                customer,
                "ordernum",
                10000L
        );
        ReflectionTestUtils.setField(order, "id", 1L);

        Payment payment = new Payment(
                10000L,
                PaymentStatus.IN_PROGRESS,
                order
        );
        ReflectionTestUtils.setField(payment, "id", 1L);

        PaymentConfirmRequest request = new PaymentConfirmRequest(
                order.getId(),
                "SUCCESS",
                20000L
        );

        given(paymentService.findByOrderIdWithOrder(anyLong())).willReturn(payment);

        //when&then
        assertThatThrownBy(() -> paymentFacade.paymentConfirm(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.PAYMENT_AMOUNT_MISMATCH.getMessage());
    }
}