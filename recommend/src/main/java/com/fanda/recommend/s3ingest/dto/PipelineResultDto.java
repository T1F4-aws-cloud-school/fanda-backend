package com.fanda.recommend.s3ingest.dto;

public record PipelineResultDto(
        String runId,
        String s3Prefix,
        String interactionsImportJobArn,
        String usersImportJobArn,
        String itemsImportJobArn,
        String solutionVersionArn,
        String campaignArn
) {
}
