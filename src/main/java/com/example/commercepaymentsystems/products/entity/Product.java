package com.example.commercepaymentsystems.products.entity;

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
    @Column(nullable = false,length = 20)
    private String name;
    @Column(nullable = false)
    private Integer price;
    @Column(nullable = false)
    private Integer stock;
    @Column(length = 500)
    private String description;
    @Enumerated(EnumType.STRING)
    private ProductCategory category;


}
