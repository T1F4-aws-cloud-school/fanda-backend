package com.fanda.auth.dto.request;

import com.fanda.auth.entity.Role;
import com.fanda.auth.entity.User;

public record UserJoinRequest(
        String username,
        String password,
        String userId
) {

    public User toEntity(String encodedPassword){
        return User.builder()
                .username(username)
                .password(encodedPassword)
                .userId(userId)
                .role(Role.USER)
                .build();
    }
}
