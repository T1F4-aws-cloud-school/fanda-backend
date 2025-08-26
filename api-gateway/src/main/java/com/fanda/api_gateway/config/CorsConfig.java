package com.fanda.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter(){
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",
                "http://192.168.2.100:*",
                "http://192.168.2.247:*",
                "https://*.elb.amazonaws.com",             // AWS Load Balancer
                "https://*.elb.us-east-1.amazonaws.com",   // US-East-1 ELB
                "http://*.elb.amazonaws.com",              // HTTP ELB (개발용)
                "http://*.elb.us-east-1.amazonaws.com",    // HTTP ELB US-East-1
                "*"
        ));
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
