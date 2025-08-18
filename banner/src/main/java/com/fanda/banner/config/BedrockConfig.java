package com.fanda.banner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

@Configuration
public class BedrockConfig {

    @Bean(name = "bedrockRuntimeClient")
    public BedrockRuntimeClient bedrockClient(){

//        System.setProperty("aws.sharedCredentialsFile", "C:\\Users\\DSO29\\.aws\\credentials");
//
//        return BedrockRuntimeClient.builder()
//                .credentialsProvider(ProfileCredentialsProvider.create("Bedrocktest"))
//                .region(Region.US_EAST_1)
//                .build();

        String region = System.getenv("AWS_DEFAULT_REGION");
        return BedrockRuntimeClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.of(region != null ? region : "us-east-1"))
                .build();
    }
}
