package com.example.commercepaymentsystems.infra.portone.controller;
import com.example.commercepaymentsystems.common.ApiResponse;
import com.example.commercepaymentsystems.infra.portone.config.PortOneProperties;
import com.example.commercepaymentsystems.infra.portone.dto.PortOneConfigResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PortOneConfigController {
    private final PortOneProperties portOneProperties;

    @GetMapping("/api/portone-info")
    public ResponseEntity<ApiResponse<?>> getPortOneInfo() {
        return ResponseEntity.ok(ApiResponse.ok(
                        new PortOneConfigResponse(
                                portOneProperties.getStoreId(),
                                portOneProperties.getChannelKey()
                        )
                )
        );
    }
}
