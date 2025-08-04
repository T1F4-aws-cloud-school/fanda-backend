package com.fanda.feedback.repository;

import com.fanda.feedback.entity.CollectedReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectedReviewRepository extends JpaRepository<CollectedReview, Long> {
    boolean existsByReviewId(Long reviewId);
    List<CollectedReview> findAllByReviewIdIn(List<Long> reviewIds);
}
