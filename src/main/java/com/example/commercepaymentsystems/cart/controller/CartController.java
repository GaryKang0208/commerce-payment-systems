package com.example.commercepaymentsystems.cart.controller;

import com.example.commercepaymentsystems.cart.dto.request.AddCartRequest;
import com.example.commercepaymentsystems.cart.dto.request.UpdateCartRequest;
import com.example.commercepaymentsystems.cart.dto.response.AddCartResponse;
import com.example.commercepaymentsystems.cart.dto.response.CartResponse;
import com.example.commercepaymentsystems.cart.service.CartService;
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
    public ResponseEntity<AddCartResponse> addItems(@AuthenticationPrincipal Long customerId,
                                                     @Valid @RequestBody AddCartRequest request){
        AddCartResponse response = cartService.addItem(customerId, request.productId(), request.quantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping()
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal Long customerId){
        return ResponseEntity.ok(cartService.getCart(customerId));
    }
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateQuantity(@AuthenticationPrincipal Long customerId,
                                               @PathVariable Long id,
                                               @Valid @RequestBody UpdateCartRequest request){
        cartService.updateQuantity(customerId, id, request.quantity());
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeItem(@AuthenticationPrincipal Long customerId,
                                           @PathVariable Long id){
        cartService.removeItem(customerId,id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> removeAllItems(@AuthenticationPrincipal Long customerId){
        cartService.removeAllItems(customerId);
        return ResponseEntity.noContent().build();
    }
}
