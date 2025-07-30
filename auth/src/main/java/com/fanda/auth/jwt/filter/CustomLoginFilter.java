package com.fanda.auth.jwt.filter;

import com.fanda.auth.jwt.dto.JwtDto;
import com.fanda.auth.jwt.userdetails.CustomUserDetails;
import com.fanda.auth.jwt.util.HttpResponseUtil;
import com.fanda.auth.jwt.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public Authentication attemptAuthentication(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response
            ) throws AuthenticationException{
        log.info("[*] Login Filter");

        Map<String, Object> requestBody;

        try{
            requestBody = getBody(request);
        } catch (IOException e){
            throw new AuthenticationServiceException("Error of request body");
        }

        log.info("[*] Request Body : "+requestBody);

        String username = (String)requestBody.get("username");
        String password = (String)requestBody.get("password");

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain,
            @NonNull Authentication authentication
            ) throws IOException {
        log.info("[*] Login Success");

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        log.info("[*] Login With "+ customUserDetails.getUsername());

        JwtDto jwtDto = new JwtDto(jwtUtil.createJwtAccessToken(customUserDetails),
                jwtUtil.createJwtRefreshToken(customUserDetails));

        HttpResponseUtil.setSuccessResponse(response, HttpStatus.CREATED, jwtDto);
    }

    public Map<String, Object> getBody(HttpServletRequest request) throws IOException{
        StringBuilder sb = new StringBuilder();
        String line;

        try(BufferedReader br = request.getReader()){
            while((line = br.readLine()) != null){
                sb.append(line);
            }
        }

        String requestBody = sb.toString();
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(requestBody, Map.class);
    }
}
