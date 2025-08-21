package com.fanda.recommend.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "personalize.s3")
public class S3IngestionProperties {

    @Getter
    @Setter
    private String bucket;

    @Getter
    @Setter
    private String basePrefix = "personalize";
}
