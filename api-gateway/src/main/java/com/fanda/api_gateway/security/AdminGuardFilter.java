package com.fanda.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

//@Component
public class AdminGuardFilter implements GlobalFilter, Ordered {

    private final SecretKey secretKey;

    public AdminGuardFilter(@Value("${jwt.secret}") String secret){
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchance, GatewayFilterChain chain){
        String path = exchance.getRequest().getURI().getPath();

        // 관리자 경로 검사
        if(path.startsWith("/feedback/api/v1/admin/")){
            String auth = exchance.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if(auth==null || !auth.startsWith("Bearer ")){
                exchance.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchance.getResponse().setComplete();
            }
            String token = auth.substring(7);

            try{
                Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
                String role = claims.get("role", String.class);
                if(!"ADMIN".equals(role)){
                    exchance.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchance.getResponse().setComplete();
                }
            } catch (Exception e){
                exchance.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchance.getResponse().setComplete();
            }
        }
        return chain.filter(exchance);
    }

    @Override
    public int getOrder(){
        return -100;
    }

}
