package com.fanda.shop.repository;

import com.fanda.shop.dto.ReviewForFeedbackDto;
import com.fanda.shop.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("""
              select new com.fanda.shop.dto.ReviewForFeedbackDto(
                r.id, r.content, r.rating, r.product.id, r.createdAt
              )
              from Review r
              where r.product.id = :productId
                and r.createdAt >= :startAt
                and r.createdAt <  :endAt
              order by r.createdAt asc
            """)
    List<ReviewForFeedbackDto> findForFeedback(
            @Param("productId") Long productId,
            @Param("startAt") java.time.LocalDateTime startAt,
            @Param("endAt")   java.time.LocalDateTime endAt
    );
}
