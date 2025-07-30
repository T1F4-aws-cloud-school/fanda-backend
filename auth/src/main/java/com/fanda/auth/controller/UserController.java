package com.fanda.auth.controller;

import com.fanda.auth.dto.request.UserJoinRequest;
import com.fanda.auth.dto.response.UserJoinResponse;
import com.fanda.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@RestController
public class UserController {

    private final UserService userService;

    @PostMapping("/join")
    public ResponseEntity<UserJoinResponse> signup(@RequestBody UserJoinRequest request){
        UserJoinResponse response = userService.join(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
