package com.example.commercepaymentsystems.order.entity;

import com.example.commercepaymentsystems.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    public Order(Customer customer, String orderNumber, Long totalPrice) {
        this.customer = customer;
        this.orderNumber = orderNumber;
        this.totalPrice = totalPrice;
        this.orderStatus = OrderStatus.PENDING_PAYMENT;
    }

    public void confirm() {
        this.orderStatus = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        this.orderStatus = OrderStatus.CANCELED;
    }
}
