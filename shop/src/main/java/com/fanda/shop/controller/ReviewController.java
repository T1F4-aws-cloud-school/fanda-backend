package com.fanda.shop.controller;

import com.fanda.shop.dto.ReviewForFeedbackDto;
import com.fanda.shop.repository.ReviewRepository;
import com.fanda.shop.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;

    @GetMapping
    public List<ReviewForFeedbackDto> getAllReviews(){
        return reviewService.getAllReviews();
    }

    @GetMapping("/by-product")
    public List<ReviewForFeedbackDto> getByProductAndRange(
            @RequestParam("productId") Long productId,
            @RequestParam("startAt") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startAt,
            @RequestParam("endAt") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endAt
            ){
        if(startAt.isAfter(endAt)){
            throw new IllegalArgumentException("startAt must be <= endAt");
        }
        LocalDateTime start = startAt.atStartOfDay();
        LocalDateTime endExclusive = endAt.plusDays(1).atStartOfDay();
        return reviewRepository.findForFeedback(productId, start, endExclusive);
    }
}
