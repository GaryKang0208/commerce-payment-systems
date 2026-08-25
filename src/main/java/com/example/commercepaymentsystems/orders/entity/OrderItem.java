package com.example.commercepaymentsystems.orders.entity;
import com.example.commercepaymentsystems.common.entity.BaseEntity;
import com.example.commercepaymentsystems.orders.entity.Order;
import com.example.commercepaymentsystems.products.entity.Product;
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
        this.productName = product.getName();
        this.productPrice = product.getPrice();
        this.quantity = quantity;
    }
}
