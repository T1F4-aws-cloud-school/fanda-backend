package com.fanda.feedback.dto;

public record ReportResponseDto(
        String positiveReportUrl,
        String negativeReportUrl
) {
}
