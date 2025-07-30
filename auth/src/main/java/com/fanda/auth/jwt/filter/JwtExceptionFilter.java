package com.fanda.auth.jwt.filter;

import com.fanda.auth.global.ApiResponse;
import com.fanda.auth.global.BaseErrorCode;
import com.fanda.auth.jwt.exception.SecurityCustomException;
import com.fanda.auth.jwt.exception.TokenErrorCode;
import com.fanda.auth.jwt.util.HttpResponseUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JwtExceptionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (SecurityCustomException e) {
            log.warn("[*] SecurityCustomException : ", e);
            BaseErrorCode errorCode = e.getErrorCode();
            ApiResponse<String> errorResponse = ApiResponse.onFailure(
                    errorCode.getCode(),
                    errorCode.getMessage(),
                    e.getMessage()
            );
            HttpResponseUtil.setErrorResponse(
                    response,
                    errorCode.getHttpStatus(),
                    errorResponse
            );
        } catch (Exception e) {
            log.error("[*] Exception : ", e);
            ApiResponse<String> errorResponse = ApiResponse.onFailure(
                    TokenErrorCode.INTERNAL_SECURITY_ERROR.getCode(),
                    TokenErrorCode.INTERNAL_SECURITY_ERROR.getMessage(),
                    e.getMessage()
            );
            HttpResponseUtil.setErrorResponse(
                    response,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    errorResponse
            );
        }
    }
}
