package com.fanda.feedback.repository;

import com.fanda.feedback.dto.ReviewForFeedbackDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "shop", url = "${SHOP_SERVICE_URL:http://localhost:8006}")
public interface ShopClient {

    @GetMapping("/api/v1/reviews/by-product")
    List<ReviewForFeedbackDto> getByProductAndRange(
            @RequestParam("productId") Long productId,
            @RequestParam("startAt") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startAt,
            @RequestParam("endAt")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endAt
    );
}
