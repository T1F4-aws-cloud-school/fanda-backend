package com.fanda.feedback.controller;

import com.fanda.feedback.dto.ReportResponseDto;
import com.fanda.feedback.service.ReviewAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class ReviewReportController {

    private final ReviewAnalysisService reviewAnalysisService;

    @PostMapping("/generate")
    public ResponseEntity<ReportResponseDto> generateReports(){
        return ResponseEntity.ok(reviewAnalysisService.generateAndUploadPdfReports());
    }
}
