package com.example.commercepaymentsystems.cart.service;

import com.example.commercepaymentsystems.cart.dto.response.CartItemResponse;
import com.example.commercepaymentsystems.cart.entity.CartItemEntity;
import com.example.commercepaymentsystems.cart.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartItemRepository cartItemRepository;
    @Transactional
    public Long addItem(CartItemEntity cartItemEntity){
        Optional<CartItemEntity> existing = cartItemRepository.findByMemberIdAndProductId(
                cartItemEntity.getMemberId(), cartItemEntity.getProductId()
        );
        if(existing.isPresent()){
            CartItemEntity found = existing.get();
            found.addQuantity(cartItemEntity.getQuantity());
            return found.getId();
        }else{
            return cartItemRepository.save(cartItemEntity).getId();
        }
    }

    public List<CartItemResponse> getCartItems(Long memberId){
        return cartItemRepository.findByMemberId(memberId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void updateQuantity(Long memberId, Long itemId, int quantity){
        CartItemEntity item = cartItemRepository.findByIdAndMemberId(itemId, memberId)
                .orElseThrow(() -> new RuntimeException("장바구니 항목을 찾을 수 없습니다."));
        item.changeQuantity(quantity);
    }

    @Transactional
    public void removeItem(Long memberId, Long itemId){
        int deleted = cartItemRepository.deleteByIdAndMemberId(itemId,memberId);
        if(deleted == 0){
            throw new RuntimeException("장바구니 항목을 찾을 수 없습니다.");
        }
    }
    private CartItemResponse toResponse(CartItemEntity item){
        return new CartItemResponse(
                item.getId(),
                item.getProductId(),
                null, //item.getProduct().getName(),
                0, //  item.getProduct().getPrice(),
                item.getQuantity(),
                0 // item.getProduct().getStock()
        );
    }
}
