package com.example.commercepaymentsystems.point.repository;
import com.example.commercepaymentsystems.point.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PointRepository extends JpaRepository<Point, Long> {

    List<Point> findByCustomersIdOrderByCreatedAtDesc(Long customerId);

    List<Point> findByPaymentId(Long paymentId);

    @Query("select coalesce(sum(pt.amount), 0) from Point pt where pt.customers.id = :customerId")
    Long sumAmountByCustomerId(@Param("customerId") Long customerId);
}
