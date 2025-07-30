package com.fanda.auth.jwt.userdetails;

import com.fanda.auth.entity.User;
import com.fanda.auth.jwt.exception.UserErrorCode;
import com.fanda.auth.jwt.exception.UserExceptionHandler;
import com.fanda.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        User user = userRepository.findByUsername(username).orElseThrow(()-> new UserExceptionHandler(UserErrorCode.USER_NOT_FOUND));

        log.info("[*] User found : "+user.getUsername());

        return new CustomUserDetails(user.getUsername(), user.getPassword(), user.getRole());
    }
}
