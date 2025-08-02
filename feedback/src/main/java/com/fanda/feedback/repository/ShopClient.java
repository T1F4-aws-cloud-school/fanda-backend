package com.fanda.feedback.repository;

import com.fanda.feedback.dto.ReviewResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "shop", url = "http://localhost:8006")
public interface ShopClient {

    @GetMapping("/reviews")
    List<ReviewResponseDto> getAllReviews();
}
