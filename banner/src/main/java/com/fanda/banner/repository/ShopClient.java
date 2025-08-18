package com.fanda.banner.repository;

import com.fanda.banner.dto.ReviewResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "shop", url = "http://fanda-shop-service.fanda-be-shop.svc.cluster.local:8006")
public interface ShopClient {

    @GetMapping("/api/v1/reviews")
    List<ReviewResponseDto> getAllReviews();

    @GetMapping("/api/v1/products/{id}/name")
    String getProductName(@PathVariable("id") Long productId);
}
