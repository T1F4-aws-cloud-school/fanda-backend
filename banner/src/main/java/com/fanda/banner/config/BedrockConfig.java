package com.fanda.banner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

@Configuration
public class BedrockConfig {

    @Value("${aws.region:${AWS_REGION:}}")
    private String region;

    private Region requireRegion() {
        if (region == null || region.isBlank()) {
            throw new IllegalStateException("AWS_REGION (or aws.region) is required");
        }
        return Region.of(region);
    }

    @Bean(name = "bedrockRuntimeClient")
    public BedrockRuntimeClient bedrockClient() {
        return BedrockRuntimeClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(requireRegion())
                .build();
    }
}
