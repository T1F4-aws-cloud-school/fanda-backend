package com.fanda.banner.repository;

import com.fanda.banner.entity.CollectedReview;
import com.fanda.banner.entity.ImprovementPhase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectedReviewRepository extends JpaRepository<CollectedReview, Long> {
    boolean existsByReviewId(Long reviewId);
    List<CollectedReview> findAllByReviewIdIn(List<Long> reviewIds);

    // BEFORE 전용 조회 메서드
    List<CollectedReview> findAllByPhase(ImprovementPhase phase);
}
