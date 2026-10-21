package com.example.commercepaymentsystems.infra.portone.webhook;

import com.example.commercepaymentsystems.common.config.SecurityConfig;
import com.example.commercepaymentsystems.common.jwt.filter.JwtAuthenticationFilter;
import com.example.commercepaymentsystems.infra.portone.config.PortOneProperties;
import io.portone.sdk.server.webhook.Webhook;
import io.portone.sdk.server.webhook.WebhookVerifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = WebhookController.class,
        excludeFilters = @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {SecurityConfig.class, JwtAuthenticationFilter.class}
        )
)
@Import({
        PortOneWebhookVerifier.class,
        PortOneProperties.class
})@TestPropertySource(properties = {
        "portone.webhook-secret=test-secret",
        "portone.api-secret=api-secret",
        "portone.channel-key=channel-key",
        "portone.base-url=baseURL",
        "portone.webhook-secret=dGVzdC13ZWJob29rLXNlY3JldC0xMjM0NTY="
})
class WebhookControllerTest {
    @MockitoBean
    private WebhookVerifier webhookVerifier;
    @MockitoBean
    private WebhookHandler webhookHandler;
    @MockitoBean
    private PortOneWebhookVerifier portOneWebhookVerifier;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    @DisplayName("포트원 웹훅 엔드포인트 테스트")
    void handlePortOneWebhook() throws Exception {
        //given
        String body = """
            {
                "type": "Transaction.Paid"
            }
            """;

        Webhook webhook = mock(Webhook.class);
        String timestamp = Instant.now().toString();

        given(portOneWebhookVerifier.verify(
                body,
                "webhook-123",
                "v1,test-signature",
                timestamp
        )).willReturn(webhook);

        //when&then
        mockMvc.perform(post("/api/webhooks/portone")
                        .with(csrf())
                        .header("webhook-id", "webhook-123")
                        .header("webhook-signature", "v1,test-signature")
                        .header("webhook-timestamp", timestamp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(webhookHandler).handle(
                "webhook-123",
                webhook,
                body
        );
    }
}