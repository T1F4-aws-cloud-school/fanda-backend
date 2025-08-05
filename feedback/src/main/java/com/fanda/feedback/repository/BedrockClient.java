package com.fanda.feedback.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BedrockClient {

    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BedrockClient(@Qualifier("bedrockRuntimeClient") BedrockRuntimeClient bedrockRuntimeClient) {
        this.bedrockRuntimeClient = bedrockRuntimeClient;
    }

    public String generate(String prompt) {
        try {
            if (prompt.length() > 4000) {
                prompt = prompt.substring(0, 4000);
            }

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            Map<String, Object> payload = new HashMap<>();
            payload.put("anthropic_version", "bedrock-2023-05-31");
            payload.put("messages", List.of(message));
            payload.put("max_tokens", 1024);
            payload.put("temperature", 0.7);
            payload.put("top_k", 250);
            payload.put("top_p", 1);

            String body = objectMapper.writeValueAsString(payload);
            System.out.println("[DEBUG] Claude 요청 JSON:\n" + body);

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId("anthropic.claude-3-sonnet-20240229-v1:0")
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(body))
                    .build();

            InvokeModelResponse response = bedrockRuntimeClient.invokeModel(request);

            String json = response.body().asUtf8String();
            System.out.println("[DEBUG] Bedrock 응답 JSON:\n" + json);

            JsonNode root = objectMapper.readTree(json);
            JsonNode contentNode = root.get("content");
            if (contentNode != null && contentNode.isArray() && contentNode.size() > 0) {
                return contentNode.get(0).get("text").asText();
            }
            return "[형식 오류] Claude 응답에서 텍스트를 찾을 수 없습니다.";
        } catch (Exception e) {
            e.printStackTrace();
            return "[예외 발생] " + e.getMessage();
        }
    }
}
