package com.example.commercepaymentsystems.products.repository;

import com.example.commercepaymentsystems.products.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product,Long> {
    Long id(Long id);

    Long id(Long id);
}
