package com.example.commercepaymentsystems.infra.portone.webhook;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebhookEventServiceTest {
    @Mock
    private WebhookEventRepository webhookEventRepository;
    @InjectMocks
    private WebhookEventService webhookEventService;

    @Test
    @DisplayName("중복 확인 후 없으면 저장")
    void saveIfNotDuplicate() {
        //given
        given(webhookEventRepository.existsByWebhookId(anyString())).willReturn(false);
        given(webhookEventRepository.save(any(WebhookEvent.class))).willReturn(new WebhookEvent());

        //when
        webhookEventService.saveIfNotDuplicate("webhookId", "type", "payload");

        //then
        verify(webhookEventRepository).save(any());
    }

    @Test
    @DisplayName("중복있으면 Optional.empty() 반환")
    void saveIfNotDuplicate_duplicate_empty() {
        //given
        given(webhookEventRepository.existsByWebhookId(anyString())).willReturn(true);

        //when&then
        assertEquals(
                Optional.empty(),
                webhookEventService.saveIfNotDuplicate("webhookId", "type", "payload")
        );
    }
}