package com.fanda.auth.jwt.dto;

public record JwtDto(
        String accessToken,
        String refreshToken
) {
}
