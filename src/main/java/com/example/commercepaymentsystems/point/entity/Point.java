package com.example.commercepaymentsystems.point.entity;
import com.example.commercepaymentsystems.common.entity.BaseEntity;
import com.example.commercepaymentsystems.customers.entity.Customers;
import com.example.commercepaymentsystems.payments.entity.Payment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "points")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customers customers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointTransactionType type;

    @Column(nullable = false)
    private Long amount;

    public Point(Customers customers, Payment payment, PointTransactionType type, Long amount) {
        this.customers = customers;
        this.payment = payment;
        this.type = type;
        this.amount = amount;
    }


}
