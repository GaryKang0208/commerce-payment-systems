package com.example.commercepaymentsystems.products.specification;

import com.example.commercepaymentsystems.products.entity.Product;
import com.example.commercepaymentsystems.products.enums.ProductCategory;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {
    public static Specification<Product> hasCategory(ProductCategory category) {
        return (root, query, criteriaBuilder) -> {
            if (category == null) {
                return null;
            }
            return criteriaBuilder.equal(
                    root.get("category"),
                    category
            );
        };
    }

    public static Specification<Product> minimumValue(Long minimumPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minimumPrice == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("price"),
                    minimumPrice
            );
        };
    }

    public static Specification<Product> maximumValue(Long maximumPrice) {
        return (root, query, criteriaBuilder) -> {
            if (maximumPrice == null) {
                return null;
            }
            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("price"),
                    maximumPrice
            );
        };
    }


}

