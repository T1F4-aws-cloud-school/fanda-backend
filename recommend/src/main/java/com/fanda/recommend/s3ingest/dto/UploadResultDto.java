package com.fanda.recommend.s3ingest.dto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UploadResultDto {
    private final String runId;
    private final String s3Prefix;
    private final String usersS3Uri;
    private final String itemsS3Uri;
    private final String interactionsS3Uri;
    private final String manifestS3Uri;

    public String getRunId() { return runId; }
    public String getS3Prefix() { return s3Prefix; }
    public String getUsersS3Uri() { return usersS3Uri; }
    public String getItemsS3Uri() { return itemsS3Uri; }
    public String getInteractionsS3Uri() { return interactionsS3Uri; }
    public String getManifestS3Uri() { return manifestS3Uri; }
}
