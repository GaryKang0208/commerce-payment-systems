package com.example.commercepaymentsystems.products.controller;

import com.example.commercepaymentsystems.common.ApiResponse;
import com.example.commercepaymentsystems.products.dto.ProductPageResponse;
import com.example.commercepaymentsystems.products.dto.ProductResponse;
import com.example.commercepaymentsystems.products.enums.ProductCategory;
import com.example.commercepaymentsystems.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<ProductPageResponse>> findAll(@RequestParam int page,
                                                                   @RequestParam int size,
                                                                   @RequestParam(required = false) ProductCategory category,
                                                                   @RequestParam(required = false) Long minimumPrice,
                                                                   @RequestParam(required = false) Long maximumPrice){
        return ResponseEntity.ok(ApiResponse.ok(productService.findAll(page,size,category,minimumPrice,maximumPrice)));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> findOne(@PathVariable Long id){
        return ResponseEntity.ok(ApiResponse.ok(productService.findOne(id)));
    }

}
