package com.fanda.banner.controller;

import com.fanda.banner.dto.ReportResponseDto;
import com.fanda.banner.service.ReviewAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class ReviewReportController {

    private final ReviewAnalysisService reviewAnalysisService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/generate")
    public ResponseEntity<ReportResponseDto> generateReports(){
        return ResponseEntity.ok(reviewAnalysisService.generateAndUploadPdfReports());
    }
}
