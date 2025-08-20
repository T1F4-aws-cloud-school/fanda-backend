package com.fanda.banner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

@Configuration
public class BedrockConfig {

    @Bean(name = "bedrockRuntimeClient")
    public BedrockRuntimeClient bedrockClient() {
        String region = System.getenv("AWS_REGION");
        if (region == null || region.isBlank()) {
            throw new IllegalStateException("AWS_REGION environment variable is required");
        }

        return BedrockRuntimeClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.of(region))
                .build();
    }
}
