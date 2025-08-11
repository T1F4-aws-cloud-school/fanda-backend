package com.fanda.feedback.repository;

import com.fanda.feedback.entity.CollectedNegativeReview;
import com.fanda.feedback.entity.ImprovementPhase;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CollectedNegativeReviewRepository extends JpaRepository<CollectedNegativeReview, Long> {

    @Query("select c.reviewId from CollectedNegativeReview c where c.reviewId in :ids and c.phase = :phase")
    List<Long> findExistingIds(@Param("ids") List<Long> ids, @Param("phase")ImprovementPhase phase);
}
