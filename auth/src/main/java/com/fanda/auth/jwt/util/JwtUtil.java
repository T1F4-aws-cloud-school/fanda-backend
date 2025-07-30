package com.fanda.auth.jwt.util;

import com.fanda.auth.jwt.exception.SecurityCustomException;
import com.fanda.auth.jwt.userdetails.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.fanda.auth.jwt.exception.TokenErrorCode.INVALID_TOKEN;
import static com.fanda.auth.jwt.exception.TokenErrorCode.TOKEN_SIGNATURE_ERROR;

@Slf4j
@Component
public class JwtUtil {

    private static final String USER_ID = "userId";
    private static final String ROLE = "role";

    private final SecretKey secretKey;
    private final Long accessExpMs;
    private final Long refreshExpMs;
    private final RedisUtil redisUtil;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.token.access-expiration-time}") Long access,
            @Value("${jwt.token.refresh-expiration-time}") Long refresh,
            RedisUtil redis
    ){
//        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
//                Jwts.SIG.HS256.key().build().getAlgorithm());
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        accessExpMs = access;
        refreshExpMs = refresh;
        redisUtil = redis;
    }

    public String createJwtAccessToken(CustomUserDetails customUserDetails){
        Instant now = Instant.now();
        return Jwts.builder()
                .setHeaderParam("alg", "HS256")
                .setHeaderParam("typ", "JWT")
                .claim(USER_ID, customUserDetails.getUsername())
                .claim(ROLE, customUserDetails.getRole().name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(accessExpMs)))
                .signWith(secretKey)
                .compact();
    }

    public String createJwtRefreshToken(CustomUserDetails customUserDetails) {
        Instant now = Instant.now();
        String refreshToken = Jwts.builder()
                .setHeaderParam("alg", "HS256")
                .setHeaderParam("typ", "JWT")
                .claim(USER_ID, customUserDetails.getUsername())
                .claim(ROLE, customUserDetails.getRole().name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(refreshExpMs)))
                .signWith(secretKey)
                .compact();

        redisUtil.saveAsValue(
                customUserDetails.getUsername() + "_refresh_token",
                refreshToken,
                refreshExpMs,
                TimeUnit.MILLISECONDS
        );
        return refreshToken;
    }

    public String resolveAccessToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.warn("[*] No Token in req");
            return null;
        }

        log.info("[*] Token exists");
        return authorization.split(" ")[1];
    }

    public String getUserId(String token) {
        return getClaims(token).get(USER_ID, String.class);
    }

    public String getRole(String token) {
        return getClaims(token).get(ROLE, String.class);
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException e) {
            throw new SecurityCustomException(INVALID_TOKEN, e);
        } catch (SignatureException e) {
            throw new SecurityCustomException(TOKEN_SIGNATURE_ERROR, e);
        }
    }
}
