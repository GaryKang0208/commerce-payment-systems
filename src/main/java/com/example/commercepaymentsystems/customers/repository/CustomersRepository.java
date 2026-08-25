package com.example.commercepaymentsystems.customers.repository;
import com.example.commercepaymentsystems.customers.entity.Customers;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomersRepository extends JpaRepository<Customers, Long> {
    Optional<Customers> findByEmail(String email);

    boolean existsByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Customers c where c.id = :id")
    Optional<Customers> findByIdForUpdate(@Param("id") Long id);
}
