package com.fanda.banner.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.File;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(File file, String key){
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("application/pdf")
                .build();

        s3Client.putObject(request, RequestBody.fromFile(file));
        return "https://"+bucketName+".s3.amazonaws.com/"+key;
    }

    public String uploadImageBytes(byte[] imageBytes, String key){
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("image/png")
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(inputStream, imageBytes.length));
        return "https://" + bucketName + ".s3.amazonaws.com/"+key;
    }
}
