package com.fanda.banner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${aws.region:${AWS_REGION:}}")
    private String region;

    private Region requireRegion() {
        if (region == null || region.isBlank()) {
            throw new IllegalStateException("AWS_REGION (or aws.region) is required");
        }
        return Region.of(region);
    }

    @Bean
    public S3Client s3Client(){
        Region r = requireRegion();

        return S3Client.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(r)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .accelerateModeEnabled(false)
                        .chunkedEncodingEnabled(true)
                        .build())
                .endpointOverride(URI.create("https://s3." + r.id() + ".amazonaws.com"))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        Region r = requireRegion();

        return S3Presigner.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(r)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .accelerateModeEnabled(false)
                        .chunkedEncodingEnabled(true)
                        .build())
                .endpointOverride(URI.create("https://s3." + r.id() + ".amazonaws.com"))
                .build();
    }
}
