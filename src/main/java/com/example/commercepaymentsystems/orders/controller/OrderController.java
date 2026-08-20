package com.example.commercepaymentsystems.orders.controller;

import com.example.commercepaymentsystems.orders.dto.request.CreateOrderRequest;
import com.example.commercepaymentsystems.orders.dto.response.CreateOrderResponse;
import com.example.commercepaymentsystems.orders.dto.response.OrderDetailResponse;
import com.example.commercepaymentsystems.orders.dto.response.OrderListResponse;
import com.example.commercepaymentsystems.orders.dto.response.OrderPreviewResponse;
import com.example.commercepaymentsystems.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    // 주문 미리보기
    // 고객의 장바구니 전체를 기준으로 현재 상품 가격과 예상 총 주문금액을 조회한다.
    // 이 단계에서는 실제 주문을 생성하지 않는다.
    @GetMapping("/preview")
    public ResponseEntity<OrderPreviewResponse> getOrderPreview(
            @RequestParam Long customerId
    ) {

        OrderPreviewResponse response =
                orderService.getOrderPreview(customerId);

        return ResponseEntity.ok(response);
    }


    // 주문 생성
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
            @RequestParam Long customerId,
            @RequestBody CreateOrderRequest request
    ) {

        CreateOrderResponse response =
                orderService.createOrder(customerId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // 내 주문 목록 조회
    @GetMapping
    public ResponseEntity<List<OrderListResponse>> getOrders(
            @RequestParam Long customerId
    ) {

        List<OrderListResponse> response =
                orderService.getOrders(customerId);

        return ResponseEntity.ok(response);
    }


    // 내 주문 상세 조회
    // orderId와 customerId를 함께 확인해서 본인의 주문만 조회할 수 있도록 한다.
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrder(
            @RequestParam Long customerId,
            @PathVariable Long orderId
    ) {

        OrderDetailResponse response =
                orderService.getOrder(customerId, orderId);

        return ResponseEntity.ok(response);
    }
}
