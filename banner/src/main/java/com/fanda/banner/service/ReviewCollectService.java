package com.fanda.banner.service;

import com.fanda.banner.dto.ReviewResponseDto;
import com.fanda.banner.entity.CollectedReview;
import com.fanda.banner.entity.ImprovementPhase;
import com.fanda.banner.repository.CollectedReviewRepository;
import com.fanda.banner.repository.ShopClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
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
                            .phase(ImprovementPhase.BEFORE)
                            .productId(review.productId())
                            .rating(review.rating())
                            .build()
            );
        }

        return newReviews;
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    public void scheduledReviewCollection() {
        List<ReviewResponseDto> collected = collectNewReviews();
        log.info("Collected {} new reviews at 08:00 AM", collected.size());
    }

    public List<ReviewResponseDto> collectNewReviewsByProductAndPhase(Long productId, ImprovementPhase phase){
        List<ReviewResponseDto> allReviews = shopClient.getAllReviews();

        List<ReviewResponseDto> filtered = new ArrayList<>();
        for(ReviewResponseDto review : allReviews){
            if(review.productId().equals(productId)){
                filtered.add(review);
            }
        }

        List<Long> reviewIds = filtered.stream().map(ReviewResponseDto::id).toList();
        List<CollectedReview> collected = collectedReviewRepository.findAllByReviewIdIn(reviewIds);
        List<Long> alreadySavedIds = collected.stream().map(CollectedReview::getReviewId).toList();

        List<ReviewResponseDto> newReviews = new ArrayList<>();
        for(ReviewResponseDto review : filtered){
            if(!alreadySavedIds.contains(review.id())){
                newReviews.add(review);
            }
        }

        for(ReviewResponseDto review : newReviews){
            collectedReviewRepository.save(
                    CollectedReview.builder()
                            .reviewId(review.id())
                            .content(review.content())
                            .collectedAt(LocalDateTime.now())
                            .phase(phase)
                            .build()
            );
        }
        return newReviews;
    }

}
