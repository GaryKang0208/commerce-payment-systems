package com.example.commercepaymentsystems.orders.controller;
import com.example.commercepaymentsystems.common.ApiResponse;
import com.example.commercepaymentsystems.orders.dto.request.CreateOrderRequest;
import com.example.commercepaymentsystems.orders.dto.request.OrderPreviewRequest;
import com.example.commercepaymentsystems.orders.dto.response.CreateOrderResponse;
import com.example.commercepaymentsystems.orders.dto.response.OrderDetailResponse;
import com.example.commercepaymentsystems.orders.dto.response.OrderListResponse;
import com.example.commercepaymentsystems.orders.dto.response.OrderPreviewResponse;
import com.example.commercepaymentsystems.orders.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<OrderPreviewResponse>> getOrderPreview(
            @AuthenticationPrincipal Long customerId,
            @RequestBody(required = false) OrderPreviewRequest request
    ) {
        OrderPreviewResponse response = orderService.getOrderPreview(customerId, request);
        return ResponseEntity.ok(ApiResponse.ok("주문 미리보기 조회 성공", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @AuthenticationPrincipal Long customerId,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        CreateOrderResponse response = orderService.createOrder(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("주문 생성 성공", response));
    }


    // 내 주문 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderListResponse>>> getOrders(
            @AuthenticationPrincipal Long customerId,
            Pageable pageable
    ) {
        Page<OrderListResponse> response = orderService.getOrders(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.ok("주문 목록 조회 성공", response));
    }


    // 내 주문 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrder(
            @AuthenticationPrincipal Long customerId,
            @PathVariable Long orderId
    ) {

        OrderDetailResponse response = orderService.getOrder(customerId, orderId);

        return ResponseEntity.ok(ApiResponse.ok("주문 상세 조회 성공", response));
    }


    // 주문 취소
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @AuthenticationPrincipal Long customerId,
            @PathVariable Long orderId
    ) {

        orderService.cancelOrder(customerId, orderId);

        return ResponseEntity.ok(ApiResponse.ok("주문 취소 성공", null));
    }
}