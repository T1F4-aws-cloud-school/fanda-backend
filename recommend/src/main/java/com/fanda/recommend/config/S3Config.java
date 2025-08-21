package com.fanda.recommend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(){
        String region = System.getenv("AWS_REGION");
        Region awsRegion = Region.of(region != null ? region : "us-east-1");
        return S3Client.builder().region(awsRegion)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
