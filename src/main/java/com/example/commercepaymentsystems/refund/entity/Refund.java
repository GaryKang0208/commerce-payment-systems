package com.example.commercepaymentsystems.refund.entity;
import com.example.commercepaymentsystems.common.entity.BaseEntity;
import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.payments.entity.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private Integer point_refund;

    @Column(nullable = false)
    private Integer pg_amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    RefundStatus status;

    @Column(name = "refund_At", nullable = false)
    LocalDateTime refundAt;

    public Refund(
            Payment payment,
            Integer pointRefund,
            Integer pgAmount,
            String reason,
            LocalDateTime refundAt
    ) {
        this.payment = payment;
        this.point_refund = pointRefund;
        this.pg_amount = pgAmount;
        this.reason = reason;
        this.status = RefundStatus.SUCCEED;
        this.refundAt = refundAt;
    }
}