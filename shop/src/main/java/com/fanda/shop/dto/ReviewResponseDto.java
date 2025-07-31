package com.fanda.shop.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponseDto(
        Long userId,
        String nickname,
        Integer rating,
        String content,
        LocalDateTime createdAt,
        List<ReviewImageDto> images
) {
}
