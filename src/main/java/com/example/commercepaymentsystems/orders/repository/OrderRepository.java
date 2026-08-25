package com.example.commercepaymentsystems.orders.repository;
import com.example.commercepaymentsystems.orders.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCustomer_IdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    Optional<Order> findByIdAndCustomer_Id(Long orderId, Long customerId);
}
