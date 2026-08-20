package com.example.commercepaymentsystems.products.entity;

import com.example.commercepaymentsystems.common.BaseEntity;
import com.example.commercepaymentsystems.products.enums.ProductCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "products")
@Getter
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false,length = 200)
    private String name;
    @Column(nullable = false)
    private Long price;
    @Column(nullable = false)
    private Integer stock;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Enumerated(EnumType.STRING)
    private ProductCategory category;


}
