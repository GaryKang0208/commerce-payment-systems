package com.example.commercepaymentsystems.infra.portone.webhook;
import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.payments.entity.PaymentStatus;
import com.example.commercepaymentsystems.payments.port.PaymentGateway;
import com.example.commercepaymentsystems.payments.port.PaymentGatewayResponse;
import com.example.commercepaymentsystems.payments.service.PaymentCommandService;
import com.example.commercepaymentsystems.payments.service.PaymentService;
import io.portone.sdk.server.webhook.Webhook;
import io.portone.sdk.server.webhook.WebhookTransactionCancelledCancelled;
import io.portone.sdk.server.webhook.WebhookTransactionPaid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebhookHandler {
    private final WebhookEventService webhookEventService;
    private final PaymentService paymentService;
    private final PaymentCommandService paymentCommandService;
    private final PaymentGateway paymentGateway;

    public void handle(String webhookId, Webhook webhook, String rawPayload) {
        String type = webhook.getClass().getSimpleName();

        //이미 받은 웹훅은 무시
        Optional<WebhookEvent> saved = webhookEventService.saveIfNotDuplicate(webhookId, type, rawPayload);
        if (saved.isEmpty()) return;
        Long eventId = saved.get().getId();

        try {
            if (webhook instanceof WebhookTransactionPaid p) {
                //결제 처리에 대한 웹훅
                handlePaid(eventId, p.getData().getPaymentId());
            } else if (webhook instanceof WebhookTransactionCancelledCancelled c) {
                //결제 취소에 대한 웹훅
                handleCancel(eventId, c.getData().getPaymentId());
            } else {
                //아니면 무시
                webhookEventService.markAsIgnored(eventId);
            }
        } catch (Exception e) {
            log.error("[Webhook] failed eventId={}", eventId, e);
            webhookEventService.markAsFailed(eventId);
        }
    }

    private void handlePaid(Long eventId, String portonePaymentId) {
        //실결제 정보 조회
        PaymentGatewayResponse pgRes = paymentGateway.getPayment(portonePaymentId);

        //결제 완료 상태가 아니면 무시
        if (!"PAID".equals(pgRes.status())) {
            webhookEventService.markAsIgnored(eventId);
            return;
        }

        //portoneId로 결제 정보 조회
        Payment payment = paymentService.findByPortOneId(portonePaymentId);

        //결제 금액 일치 확인
        if (pgRes.totalAmount() != payment.getFinalPrice()) {
            webhookEventService.markAsFailed(eventId);
            return;
        }

        //결제 완료 확인
        if (payment.getStatus() == PaymentStatus.IN_PROGRESS) {
            paymentCommandService.approvePaymentAndOrder(payment.getOrder().getId());
        }

        //웹훅 이벤트 처리 완료 마킹
        webhookEventService.markAsProcessed(eventId);
    }

    private void handleCancel(Long eventId, String portonePaymentId) {
        //실결제 정보 조회
        PaymentGatewayResponse pgRes = paymentGateway.getPayment(portonePaymentId);

        //취소 상태가 아니면 무시
        if (!"CANCELLED".equals(pgRes.status())) {
            webhookEventService.markAsIgnored(eventId);
            return;
        }

        //결제 정보 조회
        Payment payment = paymentService.findByPortOneId(portonePaymentId);

        //결제 완료 상태면 취소 진행
        if (payment.getStatus() == PaymentStatus.PAID) {
            paymentCommandService.cancelPaymentAndOrder(payment.getId());
        }

        //웹훅 이벤트 완료됨 처리
        webhookEventService.markAsProcessed(eventId);
    }
}
