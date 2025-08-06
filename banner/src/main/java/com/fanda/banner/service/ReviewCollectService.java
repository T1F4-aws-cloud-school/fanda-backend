package com.fanda.banner.service;

import com.fanda.banner.dto.ReviewResponseDto;
import com.fanda.banner.entity.CollectedReview;
import com.fanda.banner.repository.CollectedReviewRepository;
import com.fanda.banner.repository.ShopClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewCollectService {

    private final CollectedReviewRepository collectedReviewRepository;
    private final ShopClient shopClient;

    public List<ReviewResponseDto> collectNewReviews(){
        // 1. Shop 에서 전체 리뷰
        List<ReviewResponseDto> allReviews = shopClient.getAllReviews();
        // 2. 리뷰 ID 리스트 뽑기
        List<Long> reviewIds = new ArrayList<>();
        for(ReviewResponseDto review : allReviews){
            reviewIds.add(review.id());
        }
        // 3. 이미 수집한 리뷰 ID 목록
        List<CollectedReview> collectedReviews = collectedReviewRepository.findAllByReviewIdIn(reviewIds);
        List<Long> collectedIds = new ArrayList<>();
        for(CollectedReview cr : collectedReviews){
            collectedIds.add(cr.getReviewId());
        }

        // 4. 중복 아닌 리뷰
        List<ReviewResponseDto> newReviews = new ArrayList<>();
        for(ReviewResponseDto review : allReviews){
            if(!collectedIds.contains(review.id())){
                newReviews.add(review);
            }
        }

        // 5. 수집 이력 저장
        for(ReviewResponseDto review : newReviews){
            collectedReviewRepository.save(
                    CollectedReview.builder()
                            .reviewId(review.id())
                            .content(review.content())
                            .collectedAt(LocalDateTime.now())
                            .build()
            );
        }

        return newReviews;
    }

}
