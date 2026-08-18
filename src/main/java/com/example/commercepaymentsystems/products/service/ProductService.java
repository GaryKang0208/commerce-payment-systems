package com.example.commercepaymentsystems.products.service;

import com.example.commercepaymentsystems.products.dto.ProductResponse;
import com.example.commercepaymentsystems.products.entity.Product;
import com.example.commercepaymentsystems.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductService {
    public final ProductRepository productRepository;

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .toList();

    }


    public ProductResponse findOne(Long id) {
        Product product=productRepository.findById(id)
                .orElseThrow(()->new ArithmeticException(
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
