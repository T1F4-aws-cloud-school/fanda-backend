package com.fanda.banner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(){
        String region = System.getenv("AWS_DEFAULT_REGION");
        return S3Client.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.of(region != null ? region : "us-east-2"))
                .build();
//        return S3Client.builder()
//                .region(Region.US_EAST_1)
//                .credentialsProvider(ProfileCredentialsProvider.create("Bedrocktest"))
//                .build();
    }
}
