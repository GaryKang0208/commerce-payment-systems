package com.example.commercepaymentsystems.refund.repository;

import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.refund.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findAllByPaymentId(Long paymentId);

    @Query("""
        SELECT COALESCE(SUM(r.pg_amount), 0)
        FROM Refund r
        WHERE r.payment.id = :paymentId
        AND r.status = 'SUCCEED'
    """)
    Long sumRefundedAmount(
            @Param("paymentId") Long paymentId
    );
}
