package com.fanda.banner.service;

import com.fanda.banner.config.S3Uploader;
import com.fanda.banner.dto.ReportResponseDto;
import com.fanda.banner.entity.CollectedReview;
import com.fanda.banner.generator.PdfGenerator;
import com.fanda.banner.repository.BedrockClient;
import com.fanda.banner.repository.CollectedReviewRepository;
import com.fanda.banner.template.PromptTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewAnalysisService {

    private final CollectedReviewRepository collectedReviewRepository;
    private final BedrockClient bedrockClient;
    private final S3Uploader s3Uploader;

    public ReportResponseDto generateAndUploadPdfReports(){
        List<CollectedReview> reviews = collectedReviewRepository.findAll();

        String reviewText = reviews.stream().map(CollectedReview::getContent)
                .collect(Collectors.joining("\n- 리뷰: ", "\n", ""));

        String positivePrompt = PromptTemplate.getPositivePrompt(reviewText);
        String negativePrompt = PromptTemplate.getNegativePrompt(reviewText);

        String positiveReport = bedrockClient.generate(positivePrompt);
        String negativeReport = bedrockClient.generate(negativePrompt);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File positivePdf = new File("positive_"+timestamp+".pdf");
        File negativePdf = new File("negative_"+timestamp+".pdf");

        try{
            PdfGenerator.saveTextAsPdf(positiveReport, positivePdf.getPath());
            PdfGenerator.saveTextAsPdf(negativeReport, negativePdf.getPath());

            String positiveUrl = s3Uploader.uploadFile(positivePdf, "reports/positive_"+timestamp+".pdf");
            String negativeUrl = s3Uploader.uploadFile(negativePdf, "reports/negative_"+timestamp+".pdf");

            return new ReportResponseDto(positiveUrl, negativeUrl);
        }
        catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("PDF 생성 또는 S3 업로드 실패 : "+e.getMessage());
        } finally {
            positivePdf.delete();
            negativePdf.delete();
        }

    }
}
