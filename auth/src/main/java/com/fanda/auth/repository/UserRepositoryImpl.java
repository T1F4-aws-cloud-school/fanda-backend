package com.fanda.auth.repository;

import com.fanda.auth.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class UserRepositoryImpl implements UserRepository{

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findByUsername(String username){
        return userJpaRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findById(Long id){
        return userJpaRepository.findById(id);
    }

    @Override
    public Boolean existsByUsername(String username){
        return userJpaRepository.existsByUsername(username);
    }

    @Override
    public Boolean existsByUserId(String userId){
        return userJpaRepository.existsByUserId(userId);
    }

    @Override
    public User save(User user){
        return userJpaRepository.save(user);
    }
}
