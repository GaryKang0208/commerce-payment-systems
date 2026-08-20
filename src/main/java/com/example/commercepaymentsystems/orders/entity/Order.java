package com.example.commercepaymentsystems.orders.entity;

import com.example.commercepaymentsystems.common.entity.BaseEntity;
import com.example.commercepaymentsystems.common.exception.BusinessException;
import com.example.commercepaymentsystems.common.exception.ErrorCode;
import com.example.commercepaymentsystems.customers.entity.Customers;
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
    private Customers customer;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    public Order(Customers customer, String orderNumber, Long totalPrice) {
        this.customer = customer;
        this.orderNumber = orderNumber;
        this.totalPrice = totalPrice;
        this.orderStatus = OrderStatus.PENDING_PAYMENT;
    }

    public void confirm() {
        changeStatus(OrderStatus.CONFIRMED);
    }

    public void cancel() {
        changeStatus(OrderStatus.CANCELED);
    }

    private void changeStatus(OrderStatus newStatus) {
        if (!this.orderStatus.canTransitTo(newStatus)) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }

        this.orderStatus = newStatus;
    }
}
