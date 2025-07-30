package com.fanda.auth.service;

import com.fanda.auth.entity.User;
import com.fanda.auth.jwt.exception.UserExceptionHandler;
import com.fanda.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.fanda.auth.jwt.exception.UserErrorCode.USER_NOT_FOUND;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserQueryService {

    private final UserRepository userRepository;

    public Boolean checkUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public User findByUserName(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserExceptionHandler(USER_NOT_FOUND));
    }
}
