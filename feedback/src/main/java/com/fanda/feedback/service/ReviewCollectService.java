package com.fanda.feedback.service;

import com.fanda.feedback.dto.ReviewResponseDto;
import com.fanda.feedback.entity.CollectedReview;
import com.fanda.feedback.repository.CollectedReviewRepository;
import com.fanda.feedback.repository.ShopClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewCollectService {

    private final CollectedReviewRepository collectedReviewRepository;
    private final ShopClient shopClient;

    public List<ReviewResponseDto> collectNewReviews(){
        // 1. Shop 에서 전체 리뷰
        List<ReviewResponseDto> allReviews = shopClient.getAllReviews();
        // 2. 이미 수집한 리뷰 ID 목록
        List<CollectedReview> collectedReviews = collectedReviewRepository.findAllByReviewIdIn()
    }

}
