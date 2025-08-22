//package com.fanda.feedback.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.s3.S3Client;
//
////@Configuration
////public class S3Config {
////
////    @Value("${aws.region:${AWS_REGION:}}")
////    private String region;
////
////    @Bean
////    public S3Client s3Client(){
//////        String region = System.getenv("AWS_REGION");
////
////        if(region == null || region.isBlank()){
////            throw new IllegalStateException("AWS_REGION environment variable is required");
////        }
////        return S3Client.builder()
////                .credentialsProvider(DefaultCredentialsProvider.create())
////                .region(Region.of(region)).build();
////    }
////}

package com.fanda.feedback.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${aws.region:${AWS_REGION:}}")
    private String region;

    @Bean
    public S3Client s3Client(){
        if(region == null || region.isBlank()){
            throw new IllegalStateException("AWS_REGION environment variable is required");
        }

        // S3 클라이언트 고급 설정
        return S3Client.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.of(region))

                // S3 서비스 설정 추가
                .serviceConfiguration(S3Configuration.builder()
                        // Path-style 접근 활성화 (버킷명을 경로로 사용)
                        // 예: https://s3.amazonaws.com/bucket-name/key 형태
                        .pathStyleAccessEnabled(true)

                        // 가속화된 엔드포인트 비활성화 (일관된 엔드포인트 사용)
                        .accelerateModeEnabled(false)

                        // 청크 인코딩 활성화
                        .chunkedEncodingEnabled(true)
                        .build())

                // 명시적 엔드포인트 설정 (리다이렉션 방지)
                .endpointOverride(URI.create("https://s3." + region + ".amazonaws.com"))

                .build();
    }
}