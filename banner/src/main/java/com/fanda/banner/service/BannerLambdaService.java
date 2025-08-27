package com.fanda.banner.service;

import com.fanda.banner.config.S3Uploader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BannerLambdaService {

    private final LambdaClient lambdaClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final S3Uploader s3Uploader;

    @Value("${aws.lambda.bannerFunction}")
    private String bannerFunction;

    @Value("${aws.lambda.image.width:720}")
    private int defaultWidth;

    @Value("${aws.lambda.image.height:720}")
    private int defaultHeight;

    @Value("${aws.lambda.image.format:png}")
    private String defaultFormat;

    @Value("${aws.s3.presign.expireMinutes:30}")
    private int expireMinutes;

    public Result invokeGenerateBanner(String phraseKo, int index) {
        try {
            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String unique = UUID.randomUUID().toString().substring(0, 8);
            String s3Key = "banners/banner_%s_%02d_%s.%s".formatted(ts, index, unique, defaultFormat);

            Map<String,Object> payload = new HashMap<>();
            payload.put("operation", "generate-banner");
            payload.put("prompt", phraseKo);
            payload.put("out_format", defaultFormat);
            payload.put("width", defaultWidth);
            payload.put("height", defaultHeight);
            payload.put("s3_key", s3Key);

            String json = mapper.writeValueAsString(payload);

            log.debug("[BANNER] invoking lambda func={} width={} height={} format={} s3Key={}",
                    bannerFunction, defaultWidth, defaultHeight, defaultFormat, s3Key);

            InvokeRequest req = InvokeRequest.builder()
                    .functionName(bannerFunction)
                    .payload(SdkBytes.fromString(json, StandardCharsets.UTF_8))
                    .build();

            InvokeResponse resp = lambdaClient.invoke(req);
            String body = resp.payload().asUtf8String();

            log.debug("[BANNER] lambda status={} functionError={} body.sample={}",
                    resp.statusCode(), resp.functionError(),
                    (body == null ? null : body.substring(0, Math.min(body.length(), 500))));

            if (resp.functionError() != null && !resp.functionError().isBlank()) {
                throw new RuntimeException("Lambda error: " + resp.functionError() + " / body=" + body);
            }

            JsonNode root = mapper.readTree(body);
            // 람다가 s3_key를 돌려주는 걸 권장
            String returnedKey = getText(root, "s3_key");
            if (returnedKey == null || returnedKey.isBlank()) {
                // 혹시 정적 URL을 주는 경우 key를 역추출
                String rawUrl = getText(root, "imageBannerUrl");
                returnedKey = (rawUrl != null) ? extractKeyFromUrl(rawUrl) : s3Key;
            }

            log.debug("[BANNER] presign target key={}", returnedKey);
            String presigned = s3Uploader.generatePresignedGetUrl(returnedKey, Duration.ofMinutes(expireMinutes));

            String phrase = getText(root, "chatPhraseKo");
            if (phrase == null || phrase.isBlank()) phrase = phraseKo;

            return new Result(presigned, phrase, returnedKey);
        } catch (Exception e) {
            log.error("[BANNER] image generation failed: {}", e.toString(), e);
            throw new RuntimeException("Failed to invoke banner Lambda: " + e.getMessage(), e);
        }
    }

    private String extractKeyFromUrl(String url) {
        try {
            URI u = URI.create(url);
            String path = u.getPath();
            if (path.startsWith("/")) path = path.substring(1);
            return path;
        } catch (Exception e) {
            return null;
        }
    }

    private String getText(JsonNode n, String field) {
        return (n.has(field) && !n.get(field).isNull()) ? n.get(field).asText() : null;
    }

    public record Result(String imageBannerUrl, String chatPhraseKo, String s3Key) {}
}
