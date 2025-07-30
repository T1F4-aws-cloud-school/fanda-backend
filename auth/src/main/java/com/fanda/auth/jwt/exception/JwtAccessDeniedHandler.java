package com.fanda.auth.jwt.exception;



import com.fanda.auth.global.ApiResponse;
import com.fanda.auth.jwt.util.HttpResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;

@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException{
        log.warn("[*] Access Denied: ", accessDeniedException);

        HttpResponseUtil.setErrorResponse(response, HttpStatus.FORBIDDEN, ApiResponse.onFailure(
                TokenErrorCode.FORBIDDEN.getCode(),
                TokenErrorCode.FORBIDDEN.getMessage(),
                accessDeniedException.getMessage()
        ));
    }
}
