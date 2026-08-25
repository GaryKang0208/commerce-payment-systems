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

        Optional<WebhookEvent> saved = webhookEventService.saveIfNotDuplicate(webhookId, type, rawPayload);
        if (saved.isEmpty()) return;
        Long eventId = saved.get().getId();

        try {
            if (webhook instanceof WebhookTransactionPaid p) {
                handlePaid(eventId, p.getData().getPaymentId());
            } else if (webhook instanceof WebhookTransactionCancelledCancelled c) {
                handleCancel(eventId, c.getData().getPaymentId());
            } else {
                webhookEventService.markAsIgnored(eventId);
            }
        } catch (Exception e) {
            log.error("[Webhook] failed eventId={}", eventId, e);
            webhookEventService.markAsFailed(eventId);
        }
    }

    private void handlePaid(Long eventId, String portonePaymentId) {
        PaymentGatewayResponse pgRes = paymentGateway.getPayment(portonePaymentId);

        if (!"PAID".equals(pgRes.status())) {
            webhookEventService.markAsIgnored(eventId);
            return;
        }

        Payment payment = paymentService.findByPortOneId(portonePaymentId);
        if (pgRes.totalAmount() != payment.getFinalPrice()) {
            webhookEventService.markAsFailed(eventId);
            return;
        }
        if (payment.getStatus() == PaymentStatus.IN_PROGRESS) {
            paymentCommandService.approvePaymentAndOrder(payment.getOrder().getId());
        }
        webhookEventService.markAsProcessed(eventId);
    }

    private void handleCancel(Long eventId, String portonePaymentId) {
        PaymentGatewayResponse pgRes = paymentGateway.getPayment(portonePaymentId);
        if (!"CANCELLED".equals(pgRes.status())) {
            webhookEventService.markAsIgnored(eventId);
            return;
        }

        Payment payment = paymentService.findByPortOneId(portonePaymentId);
        if (payment.getStatus() == PaymentStatus.PAID) {
            paymentCommandService.cancelPaymentAndOrder(payment.getId());
        }
        webhookEventService.markAsProcessed(eventId);
    }
}
