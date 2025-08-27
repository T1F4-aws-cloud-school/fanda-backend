package com.fanda.banner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;

@Configuration
public class LambdaConfig {

    @Value("${aws.region:${AWS_REGION:}}")
    private String region;

    private Region requireRegion(){
        if(region == null || region.isBlank()){
            throw new IllegalStateException("AWS_REGION is required");
        }
        return Region.of(region);
    }

    @Bean
    public LambdaClient lambdaClient(){
        return LambdaClient.builder().region(requireRegion()).credentialsProvider(DefaultCredentialsProvider.create()).build();
    }
}
