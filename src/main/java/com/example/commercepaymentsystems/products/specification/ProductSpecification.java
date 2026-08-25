package com.example.commercepaymentsystems.products.specification;
import com.example.commercepaymentsystems.products.entity.Product;
import com.example.commercepaymentsystems.products.enums.ProductCategory;
import com.example.commercepaymentsystems.products.enums.ProductStatus;
import com.example.commercepaymentsystems.products.repository.ProductRepository;
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

    public static Specification<Product> hasSalesStatus(ProductStatus salesStatus) {
        return ((root, query, criteriaBuilder) -> {
            if (salesStatus == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("salesStatus"), salesStatus);
        });
    }

    public static Specification<Product> hasSoldOut(Boolean soldOut) {
        return ((root, query, criteriaBuilder) -> {
            if (soldOut == null) {
                return null;
            }
            if (soldOut) {
                return criteriaBuilder.equal(root.get("stock"), 0);
            }
            return criteriaBuilder.greaterThan(root.get("stock"), 0);
        });
    }
}

