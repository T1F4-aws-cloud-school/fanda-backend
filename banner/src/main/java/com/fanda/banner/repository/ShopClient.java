package com.fanda.banner.repository;

import com.fanda.banner.dto.ReviewResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "shop", url = "http://localhost:8006")
public interface ShopClient {

    @GetMapping("/api/v1/reviews")
    List<ReviewResponseDto> getAllReviews();
}
