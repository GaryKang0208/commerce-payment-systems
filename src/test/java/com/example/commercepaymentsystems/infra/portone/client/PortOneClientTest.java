package com.example.commercepaymentsystems.infra.portone.client;

import com.example.commercepaymentsystems.infra.portone.config.PortOneConfig;
import com.example.commercepaymentsystems.infra.portone.config.PortOneProperties;
import com.example.commercepaymentsystems.payments.port.PaymentGatewayResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(
        properties = {
                "portone.base-url=https://api.portone.io",
                "portone.api-secret=test-secret",
                "portone.store-id=test-store"
        }
)
@Import({
        PortOneClient.class,
        PortOneProperties.class,
        PortOneConfig.class
})
class PortOneClientTest {
    @Autowired
    private RestClient.Builder builder;
    @Autowired
    private PortOneProperties portOneProperties;
    private MockRestServiceServer mockServer;
    private PortOneClient portOneClient;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder
                .baseUrl(portOneProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "PortOne " +  portOneProperties.getApiSecret())
                .build();

        portOneClient = new PortOneClient(restClient, portOneProperties);
    }

    @Test
    @DisplayName("PG 결제 정보 조회 요청 기능 테스트")
    void getPayment() {
        //given
        mockServer.expect(requestTo("https://api.portone.io/payments/payment-123?storeId=test-store"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "PortOne test-secret"))
                .andRespond(withSuccess(
                        """
                        {
                            "id": "payment-123",
                            "status": "PAID",
                            "amount": {
                                "total": 150000
                            }
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        //when
        PaymentGatewayResponse response = portOneClient.getPayment("payment-123");

        //then
        assertEquals(150000,Long.valueOf(response.totalAmount()));
        assertEquals("PAID", response.status());
        assertEquals("payment-123", response.id());
    }

    @Test
    @DisplayName("PG 결제 취소 요청 기능 테스트")
    void cancelPayment() {
        //given
        mockServer.expect(requestTo("https://api.portone.io/payments/payment-123/cancel"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "PortOne test-secret"))
                .andExpect(header("idempotency-key", notNullValue()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(
                        """
                        {
                          "reason": "cancelled",
                          "storeId": "test-store"
                        }
                        """
                ))
                .andRespond(withSuccess());

        //when
        portOneClient.cancelPayment(
                "payment-123",
                null,
                "cancelled"
        );

        //then
        mockServer.verify();
    }
}