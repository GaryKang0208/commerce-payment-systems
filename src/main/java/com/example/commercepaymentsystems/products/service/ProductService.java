package com.example.commercepaymentsystems.products.service;

import com.example.commercepaymentsystems.common.exception.BusinessException;
import com.example.commercepaymentsystems.common.exception.ErrorCode;
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

    public ProductPageResponse findAll(int page, int size, ProductCategory category, Long minimumPrice, Long maximumPrice) {
        if (page <0){
            throw new BusinessException(ErrorCode.INVALID_PAGE);
        }//페이지
        if (size<=0||size>100){
            throw new BusinessException(ErrorCode.INVALID_PAGE_SIZE);
        }//칸
        if (minimumPrice != null && minimumPrice<0){
            throw new BusinessException(ErrorCode.INVALID_MINIMUM_PRICE);
        }
        if (maximumPrice != null && maximumPrice<0){
            throw new BusinessException(ErrorCode.INVALID_MAXIMUM_PRICE);
        }
        if (maximumPrice != null && minimumPrice != null && minimumPrice>maximumPrice){
            throw new BusinessException(ErrorCode.INVALID_PRICE_RANGE);
        }
        Pageable pageable= PageRequest.of(page,size, Sort.by(Sort.Direction.DESC,"createdAt"));
        Specification<Product> spec= ProductSpecification.hasCategory(category).and(ProductSpecification.minimumValue(minimumPrice).and(ProductSpecification.maximumValue(maximumPrice)));
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
                .orElseThrow(()->new BusinessException(ErrorCode.PRODUCT_NOT_FOUND
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
                product.getUpdatedAt(),
                product.getSalesStatus(),
                product.getStock()==0

        );
    }
}
