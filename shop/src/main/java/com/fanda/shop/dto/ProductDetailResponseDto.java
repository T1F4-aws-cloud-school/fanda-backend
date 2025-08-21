package com.fanda.shop.dto;

import java.util.List;

public record ProductDetailResponseDto(
        Long id,
        String name,
        String description,
        Integer price,
        Integer averageRating,
        String imageUrl,
        List<ReviewResponseDto> reviews
) {
}
