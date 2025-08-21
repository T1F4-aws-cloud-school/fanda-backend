package com.fanda.recommend.config;

import com.fanda.recommend.property.EtlProperties;
import com.fanda.recommend.property.S3IngestionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({EtlProperties.class, S3IngestionProperties.class})
public class RecommendConfig {
}
