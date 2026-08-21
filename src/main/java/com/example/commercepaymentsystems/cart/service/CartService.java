package com.example.commercepaymentsystems.cart.service;

import com.example.commercepaymentsystems.cart.dto.response.AddCartResponse;
import com.example.commercepaymentsystems.cart.dto.response.CartItemResponse;
import com.example.commercepaymentsystems.cart.dto.response.CartResponse;
import com.example.commercepaymentsystems.cart.entity.Cart;
import com.example.commercepaymentsystems.cart.entity.CartItem;
import com.example.commercepaymentsystems.cart.repository.CartItemRepository;
import com.example.commercepaymentsystems.cart.repository.CartRepository;
import com.example.commercepaymentsystems.common.exception.BusinessException;
import com.example.commercepaymentsystems.common.exception.ErrorCode;
import com.example.commercepaymentsystems.products.entity.Product;
import com.example.commercepaymentsystems.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {



    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public List<CartItem> findCartEntities(Long customerId) {
        return cartItemRepository.findByCart_CustomerId(customerId);
    }

    public List<CartItem> findCartEntitiesByIds(List<Long> cartItemIds, Long customerId) {
        return cartItemRepository.findByIdInAndCart_CustomerId(cartItemIds, customerId);
    }


    @Transactional
    public AddCartResponse addItem(Long customerId, Long productId, int quantity){
        Cart cart = getOrCreateCart(customerId);
                 Product product  = productRepository.findById(productId)
                .orElseThrow(()-> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        Optional<CartItem> existing = cartItemRepository.findByCart_IdAndProductId(cart.getId(), productId);
        CartItem item;
        if(existing.isPresent()) {
            item = existing.get();
            if (existing.get().getQuantity() + quantity > product.getStock()) {
                throw new BusinessException(ErrorCode.STOCK_EXCEEDED);
            }
             item.addQuantity(quantity);
        }else{
            if(quantity > product.getStock()){
                throw new BusinessException(ErrorCode.STOCK_EXCEEDED);
            }
            item = cartItemRepository.save(new CartItem(cart, productId, quantity));
        }
        return new AddCartResponse(
                item.getProductId(),
                product.getName(),
                item.getQuantity(),
                product.getPrice().intValue(),
                (int) (product.getPrice() * item.getQuantity()),
                item.getId()
        );
    }

    public CartResponse getCart(Long customerId){
        List<CartItemResponse> items = cartRepository.findByCustomerId(customerId)
                        .map(cart -> cartItemRepository.findByCart_Id(cart.getId()).stream()
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
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
        CartItem item = cartItemRepository.findByIdAndCart_Id(itemId, cart.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        Product product  = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
                if(quantity > product.getStock()){
                    throw new BusinessException(ErrorCode.STOCK_EXCEEDED);
        }
        item.changeQuantity(quantity);
    }

    @Transactional
    public void removeItem(Long customerId, Long itemId){
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));
        int deleted = cartItemRepository.deleteByIdAndCart_Id(itemId, cart.getId());
        if(deleted == 0){
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
    }

    @Transactional
    public void removeAllItems(Long customerId){
        cartRepository.findByCustomerId(customerId)
                .ifPresent(cart -> cartItemRepository.deleteByCart_Id(cart.getId()));
    }

    private Cart getOrCreateCart(Long customerId){
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> cartRepository.save(new Cart(customerId)));
    }

    private CartItemResponse toResponse(CartItem item){
        Product product  = productRepository.findById(item.getProductId())
                .orElseThrow(()-> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        long itemTotalPrice = (long) product.getPrice() * item.getQuantity();

        return new CartItemResponse(
                item.getId(),
                item.getProductId(),
                product.getName(),
                product.getPrice().intValue(),
                item.getQuantity(),
                itemTotalPrice
        );
    }
}
