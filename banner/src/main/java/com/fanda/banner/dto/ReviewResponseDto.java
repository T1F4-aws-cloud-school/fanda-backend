package com.fanda.banner.dto;

import java.time.LocalDateTime;

public record ReviewResponseDto(
        Long id,
        String content,
        Integer rating,
        Long productId,
        LocalDateTime createdAt
) {
}
