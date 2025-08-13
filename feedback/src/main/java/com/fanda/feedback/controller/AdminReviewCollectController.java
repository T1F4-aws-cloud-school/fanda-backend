package com.fanda.feedback.controller;

import com.fanda.feedback.service.CollectedNegativeReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/products")
public class AdminReviewCollectController {

    private final CollectedNegativeReviewService service;

    @PostMapping("/{productId}/reviews/collect")
    public ResponseEntity<Map<String, Object>> collect(
            @PathVariable("productId") Long productId,
            @RequestParam("startAt") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startAt,
            @RequestParam("endAt")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endAt
    ){
        int saved = service.collectForProduct(productId, startAt, endAt);
        return ResponseEntity.accepted().body(
                Map.of("productId", productId, "savedCount", saved, "startAt", startAt, "endAt", endAt)
        );
    }
}
