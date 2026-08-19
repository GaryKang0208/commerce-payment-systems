package com.example.commercepaymentsystems.cart.entity;

import com.example.commercepaymentsystems.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"cart_id", "product_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItemEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private CartEntity cart;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    public CartItemEntity(CartEntity cart, Long productId, int quantity){
        this.cart = cart;
        this.productId = productId;
        if (quantity < 1){
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        this.quantity = quantity;
    }

    public Long getCartId(){
        return cart.getId();
    }

    public void addQuantity(int quantity){
        if(quantity<1){
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        this.quantity += quantity;
    }
    public void changeQuantity(int quantity){
        if(quantity<1){
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        this.quantity = quantity;
    }

}
