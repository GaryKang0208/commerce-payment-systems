package com.example.commercepaymentsystems.payments.entity;

import com.example.commercepaymentsystems.common.entity.BaseEntity;
import com.example.commercepaymentsystems.orders.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

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

    @Column(nullable = false)
    String portoneId;
    //사용 포인트
    Long pointUsed;
    //실결제 금액
    Long pgAmount;
    //적립된 포인트
    Long savedPoints;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    Order order;

    public Payment (Long finalPrice, PaymentStatus status, Order order, Long pointUsed) {
        this.finalPrice = finalPrice;
        this.status = status;
        this.order = order;
        this.pointUsed = pointUsed;
        this.portoneId = UUID.randomUUID().toString();
        this.pgAmount = this.finalPrice - this.pointUsed;
        this.savedPoints = this.pgAmount / 100;
    }

    public static Payment create(Long finalPrice, PaymentStatus status, Order order, Long pointUsed) {
        return new Payment(finalPrice, status, order, pointUsed);
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

    public void markAsPartCancelled() {
        changeStatus(PaymentStatus.PART_CANCELLED);
    }

    private void changeStatus(PaymentStatus nextStatus) {
        if (!this.status.canTransitTo(nextStatus)) {
            throw new RuntimeException("유효하지 않은 상태 변경");
        }

        this.status = nextStatus;
    }
}
