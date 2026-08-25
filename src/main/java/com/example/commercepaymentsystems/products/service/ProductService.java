package com.example.commercepaymentsystems.products.service;
import com.example.commercepaymentsystems.common.exception.BusinessException;
import com.example.commercepaymentsystems.common.exception.ErrorCode;
import com.example.commercepaymentsystems.products.dto.ProductPageResponse;
import com.example.commercepaymentsystems.products.dto.ProductResponse;
import com.example.commercepaymentsystems.products.entity.Product;
import com.example.commercepaymentsystems.products.enums.ProductCategory;
import com.example.commercepaymentsystems.products.enums.ProductStatus;
import com.example.commercepaymentsystems.products.repository.ProductRepository;
import com.example.commercepaymentsystems.products.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ProductService {
    public final ProductRepository productRepository;

    public ProductPageResponse findAll(int page, int size, ProductCategory category, Long minimumPrice, Long maximumPrice, ProductStatus salesStatus, Boolean soldOut, String sort) {
        if (page < 0) {
            throw new BusinessException(ErrorCode.INVALID_PAGE);
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_SIZE);
        }
        if (minimumPrice != null && minimumPrice < 0) {
            throw new BusinessException(ErrorCode.INVALID_MINIMUM_PRICE);
        }
        if (maximumPrice != null && maximumPrice < 0) {
            throw new BusinessException(ErrorCode.INVALID_MAXIMUM_PRICE);
        }
        if (maximumPrice != null && minimumPrice != null && minimumPrice > maximumPrice) {
            throw new BusinessException(ErrorCode.INVALID_PRICE_RANGE);
        }

        Sort sorting;
        if (sort.equals("asc")) {
            sorting = Sort.by(Sort.Direction.ASC, "price");
        } else if (sort.equals("desc")) {
            sorting = Sort.by(Sort.Direction.DESC, "price");
        } else {
            sorting = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(page, size, sorting);
        Specification<Product> spec = ProductSpecification.hasCategory(category)
                .and(ProductSpecification.minimumValue(minimumPrice)
                        .and(ProductSpecification.maximumValue(maximumPrice)
                                .and(ProductSpecification.hasSalesStatus(salesStatus))
                                .and(ProductSpecification.hasSoldOut(soldOut))));
        Page<Product> products = productRepository.findAll(spec, pageable);
        List<ProductResponse> productResponses = products.stream()
                .map(this::toResponse)
                .toList();

        return new ProductPageResponse(
                productResponses,
                products.getTotalElements(),
                products.getNumber(),
                products.getSize()
        );
    }


    public ProductResponse findOne(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return toResponse(product);
    }

    public Product findEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getDescription(),
                product.getCategory(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getSalesStatus(),
                product.getStock() <= 0
        );
    }


}
