package com.example.commercepaymentsystems.order.entity;

import com.example.commercepaymentsystems.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_price", nullable = false)
    private Long productPrice;

    @Column(nullable = false)
    private Integer quantity;

    public OrderItem(
            Order order,
            Product product,
            Integer quantity
    ) {
        this.order = order;
        this.product = product;

        // 주문 생성 시점의 상품 정보를 스냅샷으로 저장
        this.productName = product.getName();
        this.productPrice = product.getPrice();

        this.quantity = quantity;
    }
}
