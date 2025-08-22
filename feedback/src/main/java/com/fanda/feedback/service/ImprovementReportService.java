package com.fanda.feedback.service;

import com.fanda.feedback.config.S3Uploader;
import com.fanda.feedback.dto.ReviewForFeedbackDto;
import com.fanda.feedback.generator.PdfGenerator;
import com.fanda.feedback.repository.BedrockClient;
import com.fanda.feedback.repository.ShopClient;
import com.fanda.feedback.template.PromptTemplate;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImprovementReportService {

    private final ShopClient shopClient;
    private final S3Client s3Client;
    private final S3Uploader s3Uploader;
    private final BedrockClient bedrockClient;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String generateAndUploadComparisonReport(Long productId, String baselineReportKey, LocalDate improvedStart, LocalDate improvedEnd){
        if(improvedStart == null || improvedEnd == null || improvedStart.isAfter(improvedEnd)){
            throw new IllegalArgumentException("Invalid improved date range");
        }

        // 1. s3에서 개선 전 리포트 로트 (텍스트 간주)
        String baseline = readS3Text(baselineReportKey);
        // 2. 개선 후 리뷰 조회
        List<ReviewForFeedbackDto> after = shopClient.getByProductAndRange(productId, improvedStart, improvedEnd);
        // 텍스트로 정리
        String afterText = after.stream().map(r->"- ["+r.id()+"]["+(r.rating()== null ? 0: r.rating()) + "]["+r.createdAt()+"] "+safe(r.content()))
                .collect(Collectors.joining("\n"));

        // 3. 프롬프트 구성 후 bedrock 호출, 보고서 생성
        String prompt = PromptTemplate.getComparePrompt(baseline, afterText);
        String reportMarkdown = bedrockClient.generate(prompt);

        // 4. pdf 변환
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String pdfName = "feedback_compare_"+productId + "_" + ts + ".pdf";
        File pdf = new File(pdfName);

        try{
            PdfGenerator.saveTextAsPdf(reportMarkdown, pdf.getPath());
            // 5. S3 업로드
            String key = "reports/feedback/" + pdfName;
            String url = s3Uploader.uploadPdf(pdf, key);
            return url;
        }
        catch (Exception e){
            throw new RuntimeException("비교 보고서 생성/업로드 실패: "+e.getMessage());
        }
        finally {
            if(pdf.exists()) pdf.delete();
        }
    }

    private String readS3Text(String key){
        var obj = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
        try {
            if (key.toLowerCase().endsWith(".pdf")) {
                try (PDDocument doc = PDDocument.load(obj)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    stripper.setSortByPosition(true);
                    String text = stripper.getText(doc);
                    if (text == null || text.trim().isEmpty()) {
                        throw new RuntimeException("PDF에서 텍스트를 추출하지 못했습니다: " + key);
                    }
                    return text;
                }
            } else {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(obj, StandardCharsets.UTF_8))) {
                    return br.lines().collect(Collectors.joining("\n"));
                }
            }
        } catch (Exception e){
            throw new RuntimeException("S3에서 리포트 읽기 실패: " + key, e);
        }
    }

    private String safe(String s){
        return s == null ? "" : s.replace("\n", " ").trim();
    }
}
