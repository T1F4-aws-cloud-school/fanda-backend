package com.fanda.shop.dto;

import com.fanda.shop.entity.Role;

public record UserResponseDto(
        Long id,
        String username,
        String nickname,
        Role role
) {
}