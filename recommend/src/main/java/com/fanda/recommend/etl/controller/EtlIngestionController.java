package com.fanda.recommend.etl.controller;

import com.fanda.recommend.etl.service.EtlIngestionService;
import com.fanda.recommend.s3ingest.dto.UploadResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class EtlIngestionController {

    private final EtlIngestionService service;

    @PostMapping("/internal/personalize/etl-upload")
    public ResponseEntity<UploadResultDto> trigger() {
        UploadResultDto result = service.runOnce();
        return ResponseEntity.ok(result);
    }
}
