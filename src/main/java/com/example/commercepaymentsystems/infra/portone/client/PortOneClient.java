package com.example.commercepaymentsystems.infra.portone.client;
import com.example.commercepaymentsystems.common.exception.BusinessException;
import com.example.commercepaymentsystems.common.exception.ErrorCode;
import com.example.commercepaymentsystems.infra.portone.config.PortOneProperties;
import com.example.commercepaymentsystems.infra.portone.dto.PortOneCancelRequest;
import com.example.commercepaymentsystems.infra.portone.dto.PortOnePaymentResponse;
import com.example.commercepaymentsystems.payments.port.PaymentGateway;
import com.example.commercepaymentsystems.payments.port.PaymentGatewayResponse;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class PortOneClient implements PaymentGateway {
    private final RestClient portOneRestClient;
    private final PortOneProperties portOneProperties;
    private static final int MAX_RETRIES = 3;

    @Override
    public PaymentGatewayResponse getPayment(String paymentId) {
        log.info(
                "PortOne 결제 조회: paymentId={}, storeId={}",
                paymentId,
                portOneProperties.getStoreId()
        );

        PortOnePaymentResponse response = null;
        try {
            response = portOneRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/payments/{paymentId}")
                            .queryParam("storeId", portOneProperties.getStoreId())
                            .build(paymentId))
                    .retrieve()
                    .body(PortOnePaymentResponse.class);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println("response 로그 = " + response);

        if (response == null) {
            throw new BusinessException(ErrorCode.PG_FAILURE);
        }

        return new PaymentGatewayResponse(
                response.id(),
                response.status(),
                response.amount().total()
        );
    }

    @Override
    public void cancelPayment(String paymentId, Long amount, String reason) {
        String idempotencyKey = UUID.randomUUID().toString();
        log.info("PortOne 결제 취소 요청: PortOnePaymentId={}, reason={}, idempotencyKey={}", paymentId, reason, idempotencyKey);

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                portOneRestClient.post()
                        .uri("/payments/{paymentId}/cancel", paymentId)
                        .header("idempotency-key", idempotencyKey)
                        .body(new PortOneCancelRequest(reason, amount, portOneProperties.getStoreId()))
                        .retrieve()
                        .toBodilessEntity();
                return;
            } catch (ResourceAccessException e) {
                log.warn("PortOne 취소 타임아웃 (시도 {}/{})", attempt, MAX_RETRIES);
                if (attempt == MAX_RETRIES) {
                    log.warn("[PortOne] PortOne 환불 실패, DB 적용됨, portone에서 취소 필요 PortOnePaymentId={}", paymentId);
                    throw e;
                }
            }
        }
    }
}
