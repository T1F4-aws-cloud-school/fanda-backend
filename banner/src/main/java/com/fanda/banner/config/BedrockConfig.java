package com.fanda.banner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

@Configuration
public class BedrockConfig {

    @Bean(name = "bedrockRuntimeClient")
    public BedrockRuntimeClient bedrockClient(){

        System.setProperty("aws.sharedCredentialsFile", "C:\\Users\\DSO29\\.aws\\credentials");

        return BedrockRuntimeClient.builder()
                .credentialsProvider(ProfileCredentialsProvider.create("Bedrocktest"))
                .region(Region.US_EAST_1)
                .build();
    }
}
