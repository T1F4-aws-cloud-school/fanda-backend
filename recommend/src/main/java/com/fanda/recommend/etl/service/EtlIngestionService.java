package com.fanda.recommend.etl.service;

import com.fanda.recommend.etl.EtlClient;
import com.fanda.recommend.etl.EtlZipStream;
import com.fanda.recommend.s3ingest.ZipToS3Uploader;
import com.fanda.recommend.s3ingest.dto.UploadResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EtlIngestionService {

    private final EtlClient etlClient;
    private final ZipToS3Uploader uploader;

    public UploadResultDto runOnce() {
        try (EtlZipStream zip = etlClient.runZipStream()) {
            return uploader.uploadZipToS3(zip.getZipStream(), zip.getRunId());
        } catch (Exception e) {
            throw new RuntimeException("ETL ingestion failed: " + e.getMessage(), e);
        }
    }
}
