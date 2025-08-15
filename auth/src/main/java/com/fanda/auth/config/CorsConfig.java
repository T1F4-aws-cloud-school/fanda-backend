package com.fanda.auth.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CorsConfig implements WebMvcConfigurer {

    public static CorsConfigurationSource apiConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();

        ArrayList<String> allowedOriginPatterns = new ArrayList<>();
        allowedOriginPatterns.add("http://localhost:8001");
        allowedOriginPatterns.add("http://localhost:8002");
        allowedOriginPatterns.add("http://localhost:8003");
        allowedOriginPatterns.add("http://localhost:8004");
        allowedOriginPatterns.add("http://localhost:8005");
        allowedOriginPatterns.add("http://localhost:3000");
        allowedOriginPatterns.add("http://192.168.2.100:31199");
        allowedOriginPatterns.add("http://192.168.2.247");

        ArrayList<String> allowedHttpMethods = new ArrayList<>();
        allowedHttpMethods.add("GET");
        allowedHttpMethods.add("POST");
        allowedHttpMethods.add("OPTIONS"); // CORS preflight용 필수

        configuration.setAllowedOrigins(allowedOriginPatterns);
        configuration.setAllowedMethods(allowedHttpMethods);


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
