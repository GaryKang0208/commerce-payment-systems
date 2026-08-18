package com.example.commercepaymentsystems.products.service;

import com.example.commercepaymentsystems.products.dto.ProductPageResponse;
import com.example.commercepaymentsystems.products.dto.ProductResponse;
import com.example.commercepaymentsystems.products.entity.Product;
import com.example.commercepaymentsystems.products.enums.ProductCategory;
import com.example.commercepaymentsystems.products.repository.ProductRepository;
import com.example.commercepaymentsystems.products.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductService {
    public final ProductRepository productRepository;

    public ProductPageResponse findAll(int page, int size, ProductCategory category) {
        Pageable pageable= PageRequest.of(page,size, Sort.by(Sort.Direction.DESC,"createdAt"));
        Specification<Product> spec= ProductSpecification.hasCategory(category);
        Page<Product> products=productRepository.findAll(spec,pageable);
        List<ProductResponse> productResponses= products.stream()
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
        Product product=productRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException(
                        "저장되지 않은 상품 입니다."
                ));
        return toResponse(product);
    }
    private ProductResponse toResponse(Product product){
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getDescription(),
                product.getCategory(),
                product.getCreatedAt(),
                product.getUpdatedAt()

        );
    }
}
