package com.example.commercepaymentsystems.infra.portone.webhook;
import com.example.commercepaymentsystems.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WebhookEvent extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    String webhookId;

    @Column(nullable = false)
    String eventType;

    @Enumerated(EnumType.STRING)
    WebhookStatus status;

    @Column(nullable = false, length = 500)
    String payload;

    LocalDateTime finishedAt;

    public WebhookEvent(String webhookId, String eventType, String payload) {
        this.webhookId = webhookId;
        this.eventType = eventType;
        this.status = WebhookStatus.RECEIVED;
        this.payload = payload;
    }

    public void markAsProcessed() {
        this.finishedAt = LocalDateTime.now();
        this.status = WebhookStatus.PROCESSED;
    }

    public void markAsIgnored() {
        this.finishedAt = LocalDateTime.now();
        this.status = WebhookStatus.IGNORED;
    }

    public void markAsFailed() {
        this.finishedAt = LocalDateTime.now();
        this.status = WebhookStatus.FAILED;
    }
}
