package com.fanda.banner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageGenerationService {

    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public byte[] generateImageFromPrompt(String prompt){
        try{
            Map<String, Object> requestPayload = Map.of(
                    "text_prompts", List.of(Map.of("text", prompt)),
                    "cfg_scale", 7,
                    "seed", 0,
                    "steps", 50,
                    "style_preset", "enhance"
            );
            String jsonBody = objectMapper.writeValueAsString(requestPayload);
            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId("stability.stable-diffusion-xl-v1")
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(jsonBody))
                    .build();

            InvokeModelResponse response = bedrockRuntimeClient.invokeModel(request);
            String responseJson = response.body().asUtf8String();

            JsonNode base64Node = objectMapper.readTree(responseJson).get("artifacts").get(0).get("base64");
            return Base64.getDecoder().decode(base64Node.asText());
        }
        catch (Exception e){
            throw new RuntimeException("이미지 생성 실패 : "+e.getMessage(), e);
        }
    }
}
