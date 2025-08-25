package com.fanda.feedback.config;

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

    @Bean(name = "bedrockRuntimeClient")
    public BedrockRuntimeClient bedrockRuntimeClient(){
//        String region = System.getenv("AWS_REGION");
        if(region == null || region.isBlank()){
            throw new IllegalStateException("AWS_REGION environment variable is required");
        }

        return BedrockRuntimeClient.builder().credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.of(region)).build();
    }
}