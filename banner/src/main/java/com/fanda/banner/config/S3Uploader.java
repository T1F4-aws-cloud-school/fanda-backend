package com.fanda.banner.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(File file, String key){
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
//                .contentType("application/pdf")
                .contentType(MediaType.APPLICATION_PDF_VALUE)
                .build();

        s3Client.putObject(request, RequestBody.fromFile(file));
        return "https://"+bucketName+".s3.amazonaws.com/"+key;
    }

    public String uploadImageBytes(byte[] imageBytes, String key){
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
//                .contentType("image/png")
                .contentType(MediaType.IMAGE_PNG_VALUE)
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(inputStream, imageBytes.length));
        return "https://" + bucketName + ".s3.amazonaws.com/"+key;
    }

    // presigned url
    public String generatePresignedGetUrl(String key, Duration expires){
        GetObjectRequest getReq = GetObjectRequest.builder().bucket(bucketName).key(key).build();

        GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder().signatureDuration(expires).getObjectRequest(getReq).build();

        return s3Presigner.presignGetObject(presignReq).url().toString();
    }
}
