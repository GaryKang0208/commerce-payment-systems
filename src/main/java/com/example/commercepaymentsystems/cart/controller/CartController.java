package com.example.commercepaymentsystems.cart.controller;

import com.example.commercepaymentsystems.cart.dto.request.AddCartRequest;
import com.example.commercepaymentsystems.cart.dto.request.UpdateCartRequest;
import com.example.commercepaymentsystems.cart.dto.response.AddCartResponse;
import com.example.commercepaymentsystems.cart.dto.response.CartResponse;
import com.example.commercepaymentsystems.cart.service.CartService;
import com.example.commercepaymentsystems.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<AddCartResponse>> addItems(@AuthenticationPrincipal Long customerId,
                                                     @Valid @RequestBody AddCartRequest request){
        AddCartResponse response = cartService.addItem(customerId, request.productId(), request.quantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping()
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@AuthenticationPrincipal Long customerId){
        return ResponseEntity.ok(ApiResponse.ok(cartService.getCart(customerId)));
    }
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateQuantity(@AuthenticationPrincipal Long customerId,
                                                                             @PathVariable Long id,
                                                                             @Valid @RequestBody UpdateCartRequest request){
        cartService.updateQuantity(customerId, id, request.quantity());
        return ResponseEntity.ok(ApiResponse.ok());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeItem(@AuthenticationPrincipal Long customerId,
                                           @PathVariable Long id){
        cartService.removeItem(customerId,id);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeAllItems(@AuthenticationPrincipal Long customerId){
        cartService.removeAllItems(customerId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
