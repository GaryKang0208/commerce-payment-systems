package com.example.commercepaymentsystems.infra.portone.webhook;
import com.example.commercepaymentsystems.common.exception.BusinessException;
import com.example.commercepaymentsystems.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WebhookEventService {
    private final WebhookEventRepository webhookEventRepository;

    @Transactional
    public Optional<WebhookEvent> saveIfNotDuplicate(String webhookId, String type, String payload) {
        if (webhookEventRepository.existsByWebhookId(webhookId)) {
            return Optional.empty();
        }
        WebhookEvent webhookEvent = webhookEventRepository.save(new WebhookEvent(webhookId, type, payload));
        return Optional.of(webhookEvent);
    }

    @Transactional
    public void markAsProcessed(Long eventId) {
        getWebhookEvent(eventId).markAsProcessed();
    }

    @Transactional
    public void markAsFailed(Long eventId) {
        getWebhookEvent(eventId).markAsFailed();
    }

    @Transactional
    public void markAsIgnored(Long eventId) {
        getWebhookEvent(eventId).markAsIgnored();
    }

    private WebhookEvent getWebhookEvent(Long eventId) {
        return webhookEventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WEBHOOK_NOT_FOUND));
    }
}
