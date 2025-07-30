package com.fanda.auth.dto.response;

import com.fanda.auth.entity.User;
import lombok.Builder;

@Builder
public record UserJoinResponse(
        Long id,
        String username,
        String nickname
) {

    public static UserJoinResponse from(User user){
        return UserJoinResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .build();
    }
}
