package com.example.commercepaymentsystems.infra.portone.webhook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    boolean existsByWebhookId(String Id);
}
