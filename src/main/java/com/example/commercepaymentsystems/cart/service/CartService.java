package com.example.commercepaymentsystems.cart.service;

import com.example.commercepaymentsystems.cart.dto.response.AddCartResponse;
import com.example.commercepaymentsystems.cart.dto.response.CartItemResponse;
import com.example.commercepaymentsystems.cart.dto.response.CartResponse;
import com.example.commercepaymentsystems.cart.entity.CartEntity;
import com.example.commercepaymentsystems.cart.entity.CartItemEntity;
import com.example.commercepaymentsystems.cart.repository.CartItemRepository;
import com.example.commercepaymentsystems.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private static final String DUMMY_PRODUCT_NAME = "상품명";
    private static final int DUMMY_PRICE = 10000;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public AddCartResponse addItem(Long customerId, Long productId, int quantity){
        CartEntity cart = getOrCreateCart(customerId);
        Optional<CartItemEntity> existing = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
        CartItemEntity item;
        if(existing.isPresent()){
            item = existing.get();
            item.addQuantity(quantity);
        }else{
            item = cartItemRepository.save(new CartItemEntity(cart, productId, quantity));
        }
        return new AddCartResponse(
                item.getProductId(),
                DUMMY_PRODUCT_NAME,
                item.getQuantity(),
                DUMMY_PRICE,
                DUMMY_PRICE * item.getQuantity(),
                item.getId()
        );
    }

    public CartResponse getCart(Long customerId){
        List<CartItemResponse> items = cartRepository.findByCustomerId(customerId)
                        .map(cart -> cartItemRepository.findByCartId(cart.getId()).stream()
                        .map(this::toResponse)
                        .toList())
                .orElseGet(List::of);

        int totalQuantity = 0;
        long totalPrice = 0;
        for(CartItemResponse item : items){
            totalQuantity += item.quantity();
            totalPrice += item.itemTotalPrice();
        }
        return new CartResponse(items,totalQuantity,totalPrice);
    }

    @Transactional
    public void updateQuantity(Long customerId, Long itemId, int quantity){
        CartEntity cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("장바구니를 찾을 수 없습니다."));
        CartItemEntity item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> new RuntimeException("장바구니 항목을 찾을 수 없습니다."));
        item.changeQuantity(quantity);
    }

    @Transactional
    public void removeItem(Long customerId, Long itemId){
        CartEntity cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("장바구니를 찾을 수 없습니다."));
        int deleted = cartItemRepository.deleteByIdAndCartId(itemId, cart.getId());
        if(deleted == 0){
            throw new RuntimeException("장바구니 항목을 찾을 수 없습니다.");
        }
    }

    @Transactional
    public void removeAllItems(Long customerId){
        cartRepository.findByCustomerId(customerId)
                .ifPresent(cart -> cartItemRepository.deleteByCartId(cart.getId()));
    }

    private CartEntity getOrCreateCart(Long customerId){
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> cartRepository.save(new CartEntity(customerId)));
    }

    private CartItemResponse toResponse(CartItemEntity item){
        long itemTotalPrice = (long) DUMMY_PRICE * item.getQuantity();
        return new CartItemResponse(
                item.getId(),
                item.getProductId(),
                DUMMY_PRODUCT_NAME,
                DUMMY_PRICE,
                item.getQuantity(),
                itemTotalPrice
        );
    }
}
