package com.fanda.feedback.service;

import com.fanda.feedback.dto.ReviewForFeedbackDto;
import com.fanda.feedback.entity.CollectedNegativeReview;
import com.fanda.feedback.entity.ImprovementPhase;
import com.fanda.feedback.repository.CollectedNegativeReviewRepository;
import com.fanda.feedback.repository.ShopClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectedNegativeReviewService {

    private final ShopClient shopClient;
    private final CollectedNegativeReviewRepository collectedNegativeReviewRepository;

    @Transactional
    public int collectForProduct(Long productId, ImprovementPhase phase){
        // shop에서 전체 리뷰
        List<ReviewForFeedbackDto> allReviews = shopClient.getAllReviews();
        // 필터링
        List<ReviewForFeedbackDto> reviews = allReviews.stream().filter(r -> r.productId().equals(productId)).collect(Collectors.toList());

        if(reviews.isEmpty())   return 0;

        // 이미 저장된 reviewId, phase 조회하기
        List<Long> ids = reviews.stream().map(ReviewForFeedbackDto::id).collect(Collectors.toList());

        HashSet<Long> existing = new HashSet<>(collectedNegativeReviewRepository.findExistingIds(ids, phase));

        // 새로운 데이터만 entity 변환
        List<CollectedNegativeReview> collectedNegativeReviews = reviews.stream().filter(r -> !existing.contains(r.id()))
                .map(r -> CollectedNegativeReview.builder()
                        .reviewId(r.id())
                        .content(r.content())
                        .collectedAt(LocalDateTime.now())
                        .phase(phase)
                        .productId(r.productId())
                        .rating(r.rating())
                        .build())
                .collect(Collectors.toList());

        collectedNegativeReviewRepository.saveAll(collectedNegativeReviews);
        return collectedNegativeReviews.size();
    }
}
