package com.example.commercepaymentsystems.customers.repository;
import com.example.commercepaymentsystems.customers.entity.Customers;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomersRepository extends JpaRepository<Customers , Long> {
    Optional<Customers> findByEmail(String email);
    boolean existsByEmail(String email);
}
