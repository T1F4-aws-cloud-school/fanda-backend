package com.fanda.feedback.controller;

import com.fanda.feedback.entity.ImprovementPhase;
import com.fanda.feedback.service.CollectedNegativeReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/products")
public class AdminReviewCollectController {

    private final CollectedNegativeReviewService service;

    @PostMapping("/{productId}/reviews/collect")
    public ResponseEntity<Map<String, Object>> collect(
            @PathVariable("productId") Long productId,
            @RequestParam(name = "phase", defaultValue = "AFTER") ImprovementPhase phase
            ){
        int saved = service.collectForProduct(productId, phase);
        return ResponseEntity.accepted().body(
                Map.of("productId", productId, "phase", phase, "savedCount", saved)
        );
    }
}
