package com.fanda.auth.dto.response;

import com.fanda.auth.entity.Role;

public record UserResponseDto(
        Long id,
        String username,
        String nickname,
        Role role
) {
}
