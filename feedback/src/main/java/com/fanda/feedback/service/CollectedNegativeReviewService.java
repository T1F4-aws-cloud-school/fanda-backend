package com.fanda.feedback.service;

import com.fanda.feedback.dto.ReviewForFeedbackDto;
import com.fanda.feedback.entity.CollectedNegativeReview;
import com.fanda.feedback.repository.CollectedNegativeReviewRepository;
import com.fanda.feedback.repository.ShopClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectedNegativeReviewService {

    private final ShopClient shopClient;
    private final CollectedNegativeReviewRepository collectedNegativeReviewRepository;

    @Transactional
    public int collectForProduct(Long productId, LocalDate startAt, LocalDate endAt){
        if(startAt == null || endAt == null || startAt.isAfter(endAt)){
            throw new IllegalArgumentException("Invalid date range");
        }

        List<ReviewForFeedbackDto> reviews = shopClient.getByProductAndRange(productId, startAt, endAt);

        if(reviews.isEmpty()) return 0;

        List<Long> ids = reviews.stream().map(ReviewForFeedbackDto::id).collect(Collectors.toList());
        Set<Long> existing = new HashSet<>(collectedNegativeReviewRepository.findExistingIdsAnyPhase(ids));

        List<CollectedNegativeReview> toSave = reviews.stream().filter(r -> !existing.contains(r.id())).map(r-> CollectedNegativeReview.builder()
                .reviewId(r.id())
                .content(r.content())
                .collectedAt(LocalDateTime.now())
                .productId(r.productId())
                .rating(r.rating())
                .reviewCreatedAt(r.createdAt())
                .build()).collect(Collectors.toList());

        collectedNegativeReviewRepository.saveAll(toSave);
        return toSave.size();
    }
}
