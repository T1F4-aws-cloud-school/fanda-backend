package com.fanda.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            ReactiveJwtDecoder jwtDecoder,
            ReactiveJwtAuthenticationConverter jwtAuthenticationConverter
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers(
                                "/auth/**",
                                "/actuator/**",
                                "/swagger-ui/**", "/v3/api-docs/**"
                        ).permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/banner/api/v1/images/urls").permitAll()
                        .pathMatchers("/shop/api/v1/products/**").permitAll()
                        .pathMatchers("/feedback/api/v1/admin/**").hasRole("ADMIN")
                        .pathMatchers("/feedback/api/v1/user/**").hasAnyRole("USER", "ADMIN")
                        .pathMatchers("/banner/api/v1/reports/**").hasRole("ADMIN")
                        .pathMatchers("/banner/api/v1/reviews/**").hasRole("ADMIN")
                        .pathMatchers("/feedback/api/v1/reports/**").hasRole("ADMIN")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(o -> o.jwt(j -> j
                        .jwtDecoder(jwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder(@Value("${jwt.secret}") String secret) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 48) {
            throw new IllegalStateException("JWT_SECRET must be >= 48 bytes for HS384");
        }

        SecretKeySpec key = new SecretKeySpec(secretBytes, "HmacSHA384");

        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS384)
                .build();

        return decoder;
    }

    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Object roleClaim = jwt.getClaims().get("role");
            Object rolesClaim = jwt.getClaims().get("roles");

            List<GrantedAuthority> authorities = new ArrayList<>();

            if (roleClaim instanceof String) {
                String role = ((String) roleClaim).trim();
                if (!role.isEmpty()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                }
            }
            if (rolesClaim instanceof String) {
                String[] parts = ((String) rolesClaim).split(",");
                for (String p : parts) {
                    String role = p.trim();
                    if (!role.isEmpty()) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                }
            } else if (rolesClaim instanceof Collection<?>) {
                Collection<?> coll = (Collection<?>) rolesClaim;
                for (Object r : coll) {
                    if (r != null) {
                        String role = r.toString().trim();
                        if (!role.isEmpty()) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                        }
                    }
                }
            }
            return Flux.fromIterable(authorities);
        });
        return converter;
    }
}
