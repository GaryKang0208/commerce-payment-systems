package com.example.commercepaymentsystems.payments.entity;

import com.example.commercepaymentsystems.common.BaseEntity;
import com.example.commercepaymentsystems.orders.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long finalPrice;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentStatus status;
    LocalDateTime paidAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    Order order;

    public Payment(Long finalPrice, PaymentStatus status, Order order) {
        this.finalPrice = finalPrice;
        this.status = status;
        this.order = order;
    }

    public void markAsPaid() {
        changeStatus(PaymentStatus.PAID);
        this.paidAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        changeStatus(PaymentStatus.FAILED);
    }

    public void markAsCancelled() {
        changeStatus(PaymentStatus.CANCELLED);
    }

    private void changeStatus(PaymentStatus nextStatus) {
        if (!this.status.canTransitTo(nextStatus)) {
            throw new IllegalArgumentException("유효하지 않은 상태 변경");
        }

        this.status = nextStatus;
    }
}
