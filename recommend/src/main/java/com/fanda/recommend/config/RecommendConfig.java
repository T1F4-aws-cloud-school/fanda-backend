package com.fanda.recommend.config;

import com.fanda.recommend.property.EtlProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({EtlProperties.class})
public class RecommendConfig {
}
