package com.example.commercepaymentsystems.point.controller;
import com.example.commercepaymentsystems.common.ApiResponse;
import com.example.commercepaymentsystems.point.dto.PointBalanceResponse;
import com.example.commercepaymentsystems.point.dto.PointTransactionResponse;
import com.example.commercepaymentsystems.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/points")
public class PointController {
    private final PointService pointService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PointBalanceResponse>> getBalance(
            @AuthenticationPrincipal Long customerId) {
        PointBalanceResponse response = pointService.getBalance(customerId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/me/transactions")
    public ResponseEntity<ApiResponse<List<PointTransactionResponse>>> getHistory(
            @AuthenticationPrincipal Long customerId) {
        List<PointTransactionResponse> response = pointService.getHistory(customerId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}