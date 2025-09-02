package com.fanda.api_gateway.config;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.plugins.EKSPlugin;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;  // javax -> jakarta로 변경

@Configuration
public class XRayConfig {

    @PostConstruct
    public void init() {
        AWSXRayRecorderBuilder builder = AWSXRayRecorderBuilder.standard()
                .withPlugin(new EKSPlugin());

        AWSXRay.setGlobalRecorder(builder.build());
    }
}