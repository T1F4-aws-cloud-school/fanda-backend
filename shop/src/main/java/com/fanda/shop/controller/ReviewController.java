package com.fanda.shop.controller;

import com.fanda.shop.dto.ReviewForFeedbackDto;
import com.fanda.shop.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public List<ReviewForFeedbackDto> getAllReviews(){
        return reviewService.getAllReviews();
    }
}
