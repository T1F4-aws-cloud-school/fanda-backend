package com.fanda.feedback.controller;

import com.fanda.feedback.service.ImprovementReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class ImprovementReportController {

    private final ImprovementReportService service;

    @PostMapping("/feedback/compare")
    public ResponseEntity<Map<String, Object>> generate(
            @RequestParam("productId") Long productId,
            @RequestParam("baselineKey") String baselineKey,
            @RequestParam("startAt") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate startAt,
            @RequestParam("endAt") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate endAt
            ){
        String s3Url = service.generateAndUploadComparisonReport(productId, baselineKey, startAt, endAt);
        return ResponseEntity.ok(Map.of(
                "productId", productId,
                "baselineKey", baselineKey,
                "startAt", startAt,
                "endAt", endAt,
                "reportUrl", s3Url
        ));
    }

}
