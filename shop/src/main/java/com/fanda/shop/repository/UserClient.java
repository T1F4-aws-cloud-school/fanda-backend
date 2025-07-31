package com.fanda.shop.repository;

import com.fanda.shop.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth", url = "http://localhost:8002")
public interface UserClient {

    @GetMapping("/api/v1/user/{id}")
    UserResponseDto getUser(@PathVariable("id") Long id);
}
