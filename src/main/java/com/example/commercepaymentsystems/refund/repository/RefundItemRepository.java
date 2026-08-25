package com.example.commercepaymentsystems.refund.repository;
import com.example.commercepaymentsystems.refund.entity.RefundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RefundItemRepository extends JpaRepository<RefundItem, Long> {
    List<RefundItem> findAllByOrderItemId(Long orderItemId);

    @Query("""
                SELECT COALESCE(SUM(ri.quantity), 0)
                FROM RefundItem ri
                WHERE ri.orderItem.id = :orderItemId
                AND ri.refund.status = 'SUCCEED'
            """)
    Integer sumRefundedQuantity(@Param("orderItemId") Long orderItemId);
}
