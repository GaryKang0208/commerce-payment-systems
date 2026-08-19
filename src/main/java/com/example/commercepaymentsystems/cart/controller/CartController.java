package com.example.commercepaymentsystems.cart.controller;

import com.example.commercepaymentsystems.cart.dto.request.AddCartRequest;
import com.example.commercepaymentsystems.cart.dto.request.UpdateCartRequest;
import com.example.commercepaymentsystems.cart.dto.response.AddCartResponse;
import com.example.commercepaymentsystems.cart.dto.response.CartItemResponse;
import com.example.commercepaymentsystems.cart.entity.CartItemEntity;
import com.example.commercepaymentsystems.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<AddCartResponse> addItems(@AuthenticationPrincipal Long memberId,
                                                     @Valid @RequestBody AddCartRequest request){
        CartItemEntity cartItemEntity = new CartItemEntity(memberId, request.productId(), request.quantity());
        Long cartItemId = cartService.addItem(cartItemEntity);
        return ResponseEntity.ok(new AddCartResponse(cartItemId));
    }

    @GetMapping("/items")
    public ResponseEntity<List<CartItemResponse>> getItems(@AuthenticationPrincipal Long memberId){
        return ResponseEntity.ok(cartService.getCartItems(memberId));
    }
    @PatchMapping("/items/{id}")
    public ResponseEntity<Void> updateQuantity(@AuthenticationPrincipal Long memberId,
                                               @PathVariable Long id,
                                               @Valid @RequestBody UpdateCartRequest request){
        cartService.updateQuantity(memberId,id, request.quantity());
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(@AuthenticationPrincipal Long memberId,
                                           @PathVariable Long itemId){
        cartService.removeItem(memberId,itemId);
        return ResponseEntity.ok().build();
    }
}
