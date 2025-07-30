package com.fanda.auth.repository;

import com.fanda.auth.entity.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);
    Boolean existsByUsername(String username);
    Boolean existsByNickname(String nickname);
    User save(User user);
}
