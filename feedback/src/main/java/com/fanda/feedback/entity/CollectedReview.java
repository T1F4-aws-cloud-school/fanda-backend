package com.fanda.feedback.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "collected_review")
public class CollectedReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = false)
    private Long reviewId;

    @Column(nullable = false)
    private LocalDateTime collectedAt;
}
