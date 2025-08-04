package com.fanda.feedback.controller;

import com.fanda.feedback.dto.ReviewResponseDto;
import com.fanda.feedback.service.ReviewCollectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewCollectController {

    private final ReviewCollectService reviewCollectService;

    @PostMapping("/reviews/collect")
    public ResponseEntity<List<ReviewResponseDto>> collectNewReviews(){
        List<ReviewResponseDto> newReviews = reviewCollectService.collectNewReviews();
        return ResponseEntity.ok(newReviews);
    }
}
