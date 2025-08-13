package com.fanda.feedback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "collected_negative_review")
public class CollectedNegativeReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = false)
    private Long reviewId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime collectedAt;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer rating;

    // 리뷰 작성 시간
    @Column(nullable = false)
    private LocalDateTime reviewCreatedAt;
}
