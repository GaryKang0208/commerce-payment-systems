package com.example.commercepaymentsystems.infra.portone.webhook;
import com.example.commercepaymentsystems.common.ApiResponse;
import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.webhook.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {
    private final WebhookHandler webhookHandler;
    private final PortOneWebhookVerifier portOneWebhookVerifier;

    @PostMapping("/portone")
    public ResponseEntity<ApiResponse<Void>> handlePortOneWebhook(
            @RequestHeader("webhook-id") String webhookId,
            @RequestHeader("webhook-timestamp") String webhookTimestamp,
            @RequestHeader("webhook-signature") String webhookSignature,
            @RequestBody String body
    ) {
        Webhook webhook;

        try {
            webhook = portOneWebhookVerifier.verify(body, webhookId, webhookSignature, webhookTimestamp);
        } catch (WebhookVerificationException e) {
            log.warn("[Webhook] verification failed id={}, reason={}", webhookId, e.getMessage());
            return ResponseEntity.ok(ApiResponse.ok());
        }

        webhookHandler.handle(webhookId, webhook, body);

        return ResponseEntity.ok(ApiResponse.ok());
    }
}
