package com.example.commercepaymentsystems.cart.repository;

import com.example.commercepaymentsystems.cart.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long>{
    List<CartItemEntity> findByMemberId(@Param("memberId") Long memberId);

    Optional<CartItemEntity> findByMemberIdAndProductId(Long memberId, Long productId);

    int deleteByIdAndMemberId(@Param("id") Long id,
                               @Param("memberId")
                               Long memberId);

    Optional<CartItemEntity> findByIdAndMemberId(Long id, Long MemberId);

}
