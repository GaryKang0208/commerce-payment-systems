package com.example.commercepaymentsystems.infra.portone.webhook;

import com.example.commercepaymentsystems.common.exception.BusinessException;
import com.example.commercepaymentsystems.common.exception.ErrorCode;
import com.example.commercepaymentsystems.orders.entity.Order;
import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.payments.entity.PaymentStatus;
import com.example.commercepaymentsystems.payments.port.PaymentGateway;
import com.example.commercepaymentsystems.payments.port.PaymentGatewayResponse;
import com.example.commercepaymentsystems.payments.service.PaymentCommandService;
import com.example.commercepaymentsystems.payments.service.PaymentService;
import io.portone.sdk.server.webhook.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookHandlerTest {
    @Mock
    private WebhookEventService webhookEventService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private PaymentCommandService paymentCommandService;
    @Mock
    private PaymentGateway paymentGateway;
    @InjectMocks
    private WebhookHandler webhookHandler;


    @Test
    @DisplayName("중복된 웹훅은 처리하지 않는다")
    void handle_duplicate_ignore() {
        //given
        String webhookId = "webhookId";
        String rawPayload = "{}";

        Webhook webhook = mock(Webhook.class);

        given(webhookEventService.saveIfNotDuplicate(
                anyString(),
                anyString(),
                anyString()
        )).willReturn(Optional.empty());

        //when
        webhookHandler.handle(webhookId, webhook, rawPayload);

        //then
        verifyNoInteractions(paymentService);
        verifyNoInteractions(paymentCommandService);
        verifyNoInteractions(paymentGateway);
    }

    @Test
    @DisplayName("지원하지 않는 웹훅은 무시한다.")
    void handle_unsupported_ignore() {
        //given
        String webhookId = "webhookId";
        String rawPayload = "{}";

        Webhook webhook = mock(Webhook.class);
        WebhookEvent webhookEvent = mock(WebhookEvent.class);

        given(webhookEventService.saveIfNotDuplicate(
                anyString(),
                anyString(),
                anyString()
        )).willReturn(Optional.of(webhookEvent));

        //when
        webhookHandler.handle(webhookId, webhook, rawPayload);

        //then
        verifyNoInteractions(paymentService);
        verifyNoInteractions(paymentCommandService);
        verifyNoInteractions(paymentGateway);
        verify(webhookEventService).markAsIgnored(anyLong());
    }

    @Test
    @DisplayName("결제 완료 웹훅을 처리한다.")
    void handle_paid() {
        //given
        String webhookId = "webhookId";
        String rawPayload = "{}";
        String portoneId = "portoneId";

        WebhookTransactionPaid webhook = mock(WebhookTransactionPaid.class);
        WebhookTransactionDataPaid data = mock(WebhookTransactionDataPaid.class);
        WebhookEvent webhookEvent = mock(WebhookEvent.class);

        PaymentGatewayResponse pgRes = new PaymentGatewayResponse(
                portoneId,
                "PAID",
                150_000
        );

        Payment payment = mock(Payment.class);
        Order order  = mock(Order.class);

        given(webhookEventService.saveIfNotDuplicate(
                anyString(),
                anyString(),
                anyString()
        )).willReturn(Optional.of(webhookEvent));
        given(webhookEvent.getId()).willReturn(1L);

        given(webhook.getData()).willReturn(data);
        given(data.getPaymentId()).willReturn(portoneId);
        given(paymentGateway.getPayment(portoneId)).willReturn(pgRes);
        given(paymentService.findByPortOneId(portoneId)).willReturn(payment);
        given(payment.getFinalPrice()).willReturn(150000L);
        given(payment.getStatus()).willReturn(PaymentStatus.IN_PROGRESS);
        given(payment.getOrder()).willReturn(order);
        given(order.getId()).willReturn(10L);

        //when
        webhookHandler.handle(webhookId, webhook, rawPayload);

        //then
        verify(paymentGateway).getPayment(portoneId);
        verify(paymentService).findByPortOneId(portoneId);
        verify(paymentCommandService).approvePaymentAndOrder(10L);
        verify(webhookEventService).markAsProcessed(1L);
    }

    @Test
    @DisplayName("결제 완료 웹훅이지만 PG 결제 상태가 PAID가 아니면 무시한다.")
    void handle_paid_invalidPgStatus() {
        //given
        String webhookId = "webhookId";
        String rawPayload = "{}";
        String portoneId = "portoneId";

        WebhookTransactionPaid webhook = mock(WebhookTransactionPaid.class);
        WebhookTransactionDataPaid data = mock(WebhookTransactionDataPaid.class);
        WebhookEvent webhookEvent = mock(WebhookEvent.class);

        PaymentGatewayResponse pgRes = new PaymentGatewayResponse(
                portoneId,
                "FAIL",
                150_000
        );

        given(webhookEventService.saveIfNotDuplicate(
                anyString(),
                anyString(),
                anyString()
        )).willReturn(Optional.of(webhookEvent));
        given(webhookEvent.getId()).willReturn(1L);

        given(webhook.getData()).willReturn(data);
        given(data.getPaymentId()).willReturn(portoneId);
        given(paymentGateway.getPayment(portoneId)).willReturn(pgRes);

        //when
        webhookHandler.handle(webhookId, webhook, rawPayload);

        //then
        verify(paymentGateway).getPayment(portoneId);
        verify(webhookEventService).markAsIgnored(1L);
    }

    @Test
    @DisplayName("결제 완료 웹훅의 결제 금액이 다르면 실패 처리한다.")
    void handle_paid_amountMismatch() {
        //given
        String webhookId = "webhookId";
        String rawPayload = "{}";
        String portoneId = "portoneId";

        WebhookTransactionPaid webhook = mock(WebhookTransactionPaid.class);
        WebhookTransactionDataPaid data = mock(WebhookTransactionDataPaid.class);
        WebhookEvent webhookEvent = mock(WebhookEvent.class);

        PaymentGatewayResponse pgRes = new PaymentGatewayResponse(
                portoneId,
                "PAID",
                150_000
        );

        Payment payment = mock(Payment.class);

        given(webhookEventService.saveIfNotDuplicate(
                anyString(),
                anyString(),
                anyString()
        )).willReturn(Optional.of(webhookEvent));
        given(webhookEvent.getId()).willReturn(1L);

        given(webhook.getData()).willReturn(data);
        given(data.getPaymentId()).willReturn(portoneId);
        given(paymentGateway.getPayment(portoneId)).willReturn(pgRes);
        given(paymentService.findByPortOneId(portoneId)).willReturn(payment);
        given(payment.getFinalPrice()).willReturn(15000L);

        //when
        webhookHandler.handle(webhookId, webhook, rawPayload);

        //then
        verify(paymentGateway).getPayment(portoneId);
        verify(paymentService).findByPortOneId(portoneId);
        verify(webhookEventService).markAsFailed(1L);
    }

    @Test
    @DisplayName("이미 처리된 결제는 승인하지 않고 웹훅을 무시한다.")
    void handle_paid_alreadyProcessed() {
        //given
        String webhookId = "webhookId";
        String rawPayload = "{}";
        String portoneId = "portoneId";

        WebhookTransactionPaid webhook = mock(WebhookTransactionPaid.class);
        WebhookTransactionDataPaid data = mock(WebhookTransactionDataPaid.class);
        WebhookEvent webhookEvent = mock(WebhookEvent.class);

        PaymentGatewayResponse pgRes = new PaymentGatewayResponse(
                portoneId,
                "PAID",
                150_000
        );

        Payment payment = mock(Payment.class);
        Order order  = mock(Order.class);

        given(webhookEventService.saveIfNotDuplicate(
                anyString(),
                anyString(),
                anyString()
        )).willReturn(Optional.of(webhookEvent));
        given(webhookEvent.getId()).willReturn(1L);

        given(webhook.getData()).willReturn(data);
        given(data.getPaymentId()).willReturn(portoneId);
        given(paymentGateway.getPayment(portoneId)).willReturn(pgRes);
        given(paymentService.findByPortOneId(portoneId)).willReturn(payment);
        given(payment.getFinalPrice()).willReturn(150000L);
        given(payment.getStatus()).willReturn(PaymentStatus.PAID);

        //when
        webhookHandler.handle(webhookId, webhook, rawPayload);

        //then
        verify(paymentGateway).getPayment(portoneId);
        verify(paymentService).findByPortOneId(portoneId);
        verify(webhookEventService).markAsProcessed(1L);
    }

    @Test
    @DisplayName("결제 취소 웹훅을 처리한다")
    void handle_cancel() {
        //given
        String webhookId = "webhookId";
        String rawPayload = "{}";
        String portoneId = "portoneId";

        WebhookTransactionCancelledCancelled webhook = mock(WebhookTransactionCancelledCancelled.class);
        WebhookTransactionCancelledDataCancelled data = mock(WebhookTransactionCancelledDataCancelled.class);
        WebhookEvent webhookEvent = mock(WebhookEvent.class);

        PaymentGatewayResponse pgRes = new PaymentGatewayResponse(
                portoneId,
                "CANCELLED",
                150_000
        );

        Payment payment = mock(Payment.class);

        given(webhookEventService.saveIfNotDuplicate(
                anyString(),
                anyString(),
                anyString()
        )).willReturn(Optional.of(webhookEvent));
        given(webhookEvent.getId()).willReturn(1L);

        given(webhook.getData()).willReturn(data);
        given(data.getPaymentId()).willReturn(portoneId);
        given(paymentGateway.getPayment(portoneId)).willReturn(pgRes);
        given(paymentService.findByPortOneId(portoneId)).willReturn(payment);
        given(payment.getStatus()).willReturn(PaymentStatus.PAID);
        given(payment.getId()).willReturn(1L);

        //when
        webhookHandler.handle(webhookId, webhook, rawPayload);

        //then
        verify(paymentGateway).getPayment(portoneId);
        verify(paymentService).findByPortOneId(portoneId);
        verify(paymentCommandService).cancelPaymentAndOrder(payment.getId());
        verify(webhookEventService).markAsProcessed(1L);
    }

    @Test
    @DisplayName("결제 취소 웹훅이지만 PG 상태가 CANCELLED가 아니면 무시한다")
    void handle_cancel_invalidPGStatus() {
        //given
        String webhookId = "webhookId";
        String rawPayload = "{}";
        String portoneId = "portoneId";

        WebhookTransactionCancelledCancelled webhook = mock(WebhookTransactionCancelledCancelled.class);
        WebhookTransactionCancelledDataCancelled data = mock(WebhookTransactionCancelledDataCancelled.class);
        WebhookEvent webhookEvent = mock(WebhookEvent.class);

        PaymentGatewayResponse pgRes = new PaymentGatewayResponse(
                portoneId,
                "PAID",
                150_000
        );

        given(webhookEventService.saveIfNotDuplicate(
                anyString(),
                anyString(),
                anyString()
        )).willReturn(Optional.of(webhookEvent));
        given(webhookEvent.getId()).willReturn(1L);

        given(webhook.getData()).willReturn(data);
        given(data.getPaymentId()).willReturn(portoneId);
        given(paymentGateway.getPayment(portoneId)).willReturn(pgRes);

        //when
        webhookHandler.handle(webhookId, webhook, rawPayload);

        //then
        verify(paymentGateway).getPayment(portoneId);
        verify(webhookEventService).markAsIgnored(1L);
    }

    @Test
    @DisplayName("이미 처리된 결제 취소는 취소 명령을 실행하지 않는다")
    void handle_cancel_alreadyCancelled() {
        //given
        String webhookId = "webhookId";
        String rawPayload = "{}";
        String portoneId = "portoneId";

        WebhookTransactionCancelledCancelled webhook = mock(WebhookTransactionCancelledCancelled.class);
        WebhookTransactionCancelledDataCancelled data = mock(WebhookTransactionCancelledDataCancelled.class);
        WebhookEvent webhookEvent = mock(WebhookEvent.class);

        PaymentGatewayResponse pgRes = new PaymentGatewayResponse(
                portoneId,
                "CANCELLED",
                150_000
        );

        Payment payment = mock(Payment.class);

        given(webhookEventService.saveIfNotDuplicate(
                anyString(),
                anyString(),
                anyString()
        )).willReturn(Optional.of(webhookEvent));
        given(webhookEvent.getId()).willReturn(1L);

        given(webhook.getData()).willReturn(data);
        given(data.getPaymentId()).willReturn(portoneId);
        given(paymentGateway.getPayment(portoneId)).willReturn(pgRes);
        given(paymentService.findByPortOneId(portoneId)).willReturn(payment);
        given(payment.getStatus()).willReturn(PaymentStatus.CANCELLED);

        //when
        webhookHandler.handle(webhookId, webhook, rawPayload);

        //then
        verify(paymentGateway).getPayment(portoneId);
        verify(paymentService).findByPortOneId(portoneId);
        verifyNoInteractions(paymentCommandService);
        verify(webhookEventService).markAsProcessed(1L);
    }

    @Test
    @DisplayName("웹훅 처리 중 예외가 발생하면 실패 처리한다.")
    void handle_exceptionThrown_then_fail() {
        //given
        String webhookId = "webhookId";
        String rawPayload = "{}";
        String portoneId = "portoneId";

        WebhookTransactionPaid webhook = mock(WebhookTransactionPaid.class);
        WebhookTransactionDataPaid data = mock(WebhookTransactionDataPaid.class);
        WebhookEvent webhookEvent = mock(WebhookEvent.class);

        PaymentGatewayResponse pgRes = new PaymentGatewayResponse(
                portoneId,
                "PAID",
                150_000
        );

        given(webhookEventService.saveIfNotDuplicate(
                anyString(),
                anyString(),
                anyString()
        )).willReturn(Optional.of(webhookEvent));
        given(webhookEvent.getId()).willReturn(1L);

        given(webhook.getData()).willReturn(data);
        given(data.getPaymentId()).willReturn(portoneId);
        given(paymentGateway.getPayment(portoneId)).willReturn(pgRes);
        given(paymentService.findByPortOneId(portoneId)).willThrow(new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        //when
        webhookHandler.handle(webhookId, webhook, rawPayload);

        //then
        verify(paymentGateway).getPayment(portoneId);
        verify(webhookEventService).markAsFailed(1L);
    }
}