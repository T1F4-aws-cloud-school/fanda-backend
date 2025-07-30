package com.fanda.auth.jwt.exception;

import com.fanda.auth.global.ApiResponse;
import com.fanda.auth.jwt.util.HttpResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException {
        HttpStatus httpStatus;
        ApiResponse<String> errorResponse;

        log.error("[*] AuthenticationException: ", authException);
        httpStatus = HttpStatus.UNAUTHORIZED;
        errorResponse = ApiResponse.onFailure(
                TokenErrorCode.INVALID_TOKEN.getCode(),
                TokenErrorCode.INVALID_TOKEN.getMessage(),
                authException.getMessage());

        HttpResponseUtil.setErrorResponse(response, httpStatus, errorResponse);
    }
}
