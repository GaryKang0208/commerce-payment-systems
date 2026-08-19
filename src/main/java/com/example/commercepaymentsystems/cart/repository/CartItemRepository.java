package com.example.commercepaymentsystems.cart.repository;

import com.example.commercepaymentsystems.cart.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long>{
    List<CartItemEntity> findByCartId(@Param("cartId") Long cartId);

    Optional<CartItemEntity> findByCartIdAndProductId(Long cartId, Long productId);

    int deleteByIdAndCartId(@Param("id") Long id,
                             @Param("cartId")
                             Long cartId);

    Optional<CartItemEntity> findByIdAndCartId(Long id, Long cartId);

    void deleteByCartId(@Param("cartId") Long cartId);

}
