package com.fanda.shop.service;

import com.fanda.shop.dto.ReviewForFeedbackDto;
import com.fanda.shop.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public List<ReviewForFeedbackDto> getAllReviews(){
        return reviewRepository.findAll().stream()
                .map(review -> new ReviewForFeedbackDto(
                        review.getId(),
                        review.getContent(),
                        review.getRating(),
                        review.getProduct().getId(),
                        review.getCreatedAt()
                )).toList();
    }
}
