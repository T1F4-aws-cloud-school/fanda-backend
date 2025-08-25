package com.fanda.banner.controller;

import com.fanda.banner.dto.ReportResponseDto;
import com.fanda.banner.service.ReviewAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
public class ReviewReportController {

    private final ReviewAnalysisService reviewAnalysisService;

    @PostMapping("/generate")
    public ResponseEntity<List<ReportResponseDto>> generateReports(){
        return ResponseEntity.ok(reviewAnalysisService.generateAndUploadPdfReports());
    }
}
