package com.fanda.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    @Primary
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 오리진 설정
        List<String> allowedOrigins = Arrays.asList(
                "http://localhost:8001",
                "http://localhost:8002",
                "http://localhost:8003",
                "http://localhost:8004",
                "http://localhost:8005",
                "http://localhost:3000",
                "http://192.168.2.100:31199",
                "http://192.168.2.247"
        );

        configuration.setAllowedOrigins(allowedOrigins);

        // 허용할 HTTP 메소드
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // 허용할 헤더 - 이 부분이 중요합니다!
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 인증 정보 포함 허용
        configuration.setAllowCredentials(true);

        // preflight 요청 캐시 시간
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}