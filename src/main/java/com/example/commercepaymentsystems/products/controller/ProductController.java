package com.example.commercepaymentsystems.products.controller;

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
    public ResponseEntity<ProductPageResponse> findAll(@RequestParam int page,
                                                       @RequestParam int size,
                                                       @RequestParam(required = false) ProductCategory category,
                                                       @RequestParam(required = false) Integer minimumPrice,
                                                       @RequestParam(required = false) Integer maximumPrice){
        return ResponseEntity.ok(productService.findAll(page,size,category,minimumPrice,maximumPrice));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findOne(@PathVariable Long id){
        return ResponseEntity.ok(productService.findOne(id));
    }

}
