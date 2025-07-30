package com.fanda.auth.config;

import com.fanda.auth.jwt.exception.JwtAccessDeniedHandler;
import com.fanda.auth.jwt.exception.JwtAuthenticationEntryPoint;
import com.fanda.auth.jwt.filter.CustomLoginFilter;
import com.fanda.auth.jwt.filter.JwtAuthenticationFilter;
import com.fanda.auth.jwt.filter.JwtExceptionFilter;
import com.fanda.auth.jwt.util.JwtUtil;
import com.fanda.auth.jwt.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;
import java.util.stream.Stream;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final String[] swaggerUrls = {"/swagger-ui/**", "/v3/**"};
    private final String[] authUrls = {"/", "/api/v1/user/join/**", "/api/v1/user/login/**", "/api/v1/redis/**"};
    private final String[] allowedUrls = Stream.concat(Arrays.stream(swaggerUrls), Arrays.stream(authUrls))
            .toArray(String[]::new);
    private final AuthenticationConfiguration authenticationConfiguration;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CorsConfig
        http.cors(cors -> cors.configurationSource(CorsConfig.apiConfigurationSource()));
        // csrf disable
        http.csrf(AbstractHttpConfigurer::disable);
        // form 로그인 방식 disable
        http.formLogin(AbstractHttpConfigurer::disable);
        // http basic 인증 방식 disable
        http.httpBasic(AbstractHttpConfigurer::disable);
        // session 사용 X, Stateless 서버
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // 경로별 인가 작업
        http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
                .requestMatchers("/user/**").authenticated()
                .requestMatchers("/admin/**").hasAnyRole("ADMIN")
                .requestMatchers(allowedUrls).permitAll()
                .anyRequest().permitAll()
        );
        // Jwt Filter (with login)
        CustomLoginFilter loginFilter = new CustomLoginFilter(
                authenticationManager(authenticationConfiguration), jwtUtil
        );
        loginFilter.setFilterProcessesUrl("/api/v1/user/login");

        http
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        // JwtExceptionFilter 사용
        http
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, redisUtil), CustomLoginFilter.class);

        http
                .addFilterBefore(new JwtExceptionFilter(), JwtAuthenticationFilter.class);

        return http.build();
    }
}
