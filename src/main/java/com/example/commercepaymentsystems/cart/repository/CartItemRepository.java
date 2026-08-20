package com.example.commercepaymentsystems.cart.repository;

import com.example.commercepaymentsystems.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long>{
    List<CartItem> findByCartId(Long cartId);

    List<CartItem> findByCart_CustomerId(Long customerId);
    List<CartItem> findByIdInAndCart_CustomerId(List<Long> ids, Long customerId);

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    int deleteByIdAndCartId(@Param("id") Long id,
                             @Param("cartId")
                             Long cartId);

    Optional<CartItem> findByIdAndCartId(Long id, Long cartId);

    void deleteByCartId(@Param("cartId") Long cartId);

}
