package com.fanda.shop.controller;

import com.fanda.shop.dto.ProductDetailResponseDto;
import com.fanda.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/products")
@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public ProductDetailResponseDto getProductDetail(@PathVariable("id") Long id){
        return productService.getProductDetail(id);
    }
}
