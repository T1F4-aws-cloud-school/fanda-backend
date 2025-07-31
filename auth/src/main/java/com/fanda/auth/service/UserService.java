package com.fanda.auth.service;

import com.fanda.auth.dto.request.UserJoinRequest;
import com.fanda.auth.dto.response.UserJoinResponse;
import com.fanda.auth.dto.response.UserResponseDto;
import com.fanda.auth.entity.User;
import com.fanda.auth.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserJoinResponse join(UserJoinRequest request){
        final User user = request.toEntity(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        return UserJoinResponse.from(user);
    }

    public UserResponseDto getUser(Long id){
        User user = userRepository.findById(id).orElseThrow(()-> new NoSuchElementException("유저를 찾을 수 없습니다. id : "+id));
        return new UserResponseDto(user.getId(), user.getUsername(), user.getNickname(), user.getRole());
    }
}
