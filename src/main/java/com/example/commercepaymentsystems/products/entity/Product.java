package com.example.commercepaymentsystems.products.entity;

import com.example.commercepaymentsystems.common.BaseEntity;
import com.example.commercepaymentsystems.products.enums.ProductCategory;
import com.example.commercepaymentsystems.products.enums.ProductStatus;
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
    @Enumerated(EnumType.STRING)
    @Column(name = "sales_status",nullable = false)
    private ProductStatus salesStatus;

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("차감 수량은 1개 이상이어야 합니다.");
        }
        if (this.stock < quantity) {
            throw new IllegalArgumentException("상품 재고가 부족합니다.");
        }
        this.stock -= quantity;
    } //주문쪽에서 요청하신 코드

    public void restoreStock(int quantity) {
        this.stock += quantity;
    }// 결제 파트 요청코드
}
