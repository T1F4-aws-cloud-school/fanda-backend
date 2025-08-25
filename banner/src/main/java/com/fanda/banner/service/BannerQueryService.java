package com.fanda.banner.service;

import com.fanda.banner.dto.BannerItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerQueryService {

    private static final Duration PRESIGNED_TTL = Duration.ofMinutes(15);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public List<BannerItemDto> listAllBannerUrls(){
        List<BannerItemDto> result = new ArrayList<>();

        String token = null;
        do{
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucket).prefix("banners/").continuationToken(token).maxKeys(1000).build();

            ListObjectsV2Response res = s3Client.listObjectsV2(listRequest);
            for(S3Object object : res.contents()){
                if(!object.key().endsWith("/")){
                    GetObjectRequest getReq = GetObjectRequest.builder().bucket(bucket).key(object.key()).build();
                    GetObjectPresignRequest presignReq = GetObjectPresignRequest.builder().signatureDuration(PRESIGNED_TTL).getObjectRequest(getReq).build();
                    PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignReq);
                    String url = presigned.url().toString();
                    result.add(new BannerItemDto(url));
                }
            }
            token = res.isTruncated() ? res.nextContinuationToken() : null;
        } while(token != null);

        return result;
    }
}
