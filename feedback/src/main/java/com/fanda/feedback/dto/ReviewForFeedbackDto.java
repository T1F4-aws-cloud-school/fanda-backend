package com.fanda.feedback.dto;

import java.time.LocalDateTime;

public record ReviewForFeedbackDto(
        Long id,
        String content,
        Integer rating,
        Long productId,
        LocalDateTime createdAt
) {
}
