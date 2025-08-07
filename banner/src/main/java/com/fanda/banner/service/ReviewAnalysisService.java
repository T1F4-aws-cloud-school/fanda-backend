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
    private final ImageGenerationService imageGenerationService;

    public ReportResponseDto generateAndUploadPdfReports(){
        List<CollectedReview> reviews = collectedReviewRepository.findAll();

        String reviewText = reviews.stream().map(CollectedReview::getContent)
                .collect(Collectors.joining("\n- 리뷰: ", "\n", ""));

        // 1. 프롬프트 생성
        String positivePrompt = PromptTemplate.getPositivePrompt(reviewText);
        String negativePrompt = PromptTemplate.getNegativePrompt(reviewText);

        // 2. 리포트 생성
        String positiveReport = bedrockClient.generate(positivePrompt);
        String negativeReport = bedrockClient.generate(negativePrompt);

        // 3. catchphrase, 상품명 추출
        String catchPhraseKo = extractCatchPhrase(positiveReport);
        String productName = extractProductName(positiveReport);

        // 4-1. 영어로 번역
        String catchPhraseEn = bedrockClient.generate("""
            Translate the following Korean marketing phrase into fluent English for an online shopping banner:
            """ + catchPhraseKo).replace("\n", "").trim();

        // 4-2. 배너 이미지 생성용 프롬프트
        //String bannerPrompt = PromptTemplate.getBannerImagePrompt(catchPhraseEn);
        String bannerPrompt = String.format("""
            Create a banner image (750x320 px) for a shopping app.
        
            - Product: %s
            - Include the phrase: "%s" in bold, stylish white text centered on the image.
            - Use a premium and minimal background that fits the product's characteristics.
            - No logos, no borders, no icons.
        """, productName, catchPhraseEn);

        // 5. Stability로 이미지 생성
        byte[] imageBytes = imageGenerationService.generateImageFromPrompt(bannerPrompt);

        // 6. timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        // 7. 파일명 생성
        String imageKey = "banners/banner_"+timestamp+".png";
        String positiveKey = "reports/positive/positive_" + timestamp + ".pdf";
        String negativeKey = "reports/negative/negative_" + timestamp + ".pdf";

        // s3 업로드
        String imageUrl = s3Uploader.uploadImageBytes(imageBytes, imageKey);
        File positivePdf = new File("positive_"+timestamp+".pdf");
        File negativePdf = new File("negative_"+timestamp+".pdf");

        try{
            PdfGenerator.saveTextAsPdf(positiveReport, positivePdf.getPath());
            PdfGenerator.saveTextAsPdf(negativeReport, negativePdf.getPath());

            String positiveUrl = s3Uploader.uploadFile(positivePdf, positiveKey);
            String negativeUrl = s3Uploader.uploadFile(negativePdf, negativeKey);

            return new ReportResponseDto(imageUrl, catchPhraseKo);
        }
        catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("PDF 생성 또는 S3 업로드 실패 : "+e.getMessage());
        } finally {
            positivePdf.delete();
            negativePdf.delete();
        }

    }

    private String extractCatchPhrase(String text){
        for(String line : text.split("\n")){
            line = line.trim();
            if(line.matches("^(\\d+\\.|[-*•])\\s?.+")){
                return line.replaceFirst("^(\\d+\\.|[-*•])\\s?", "");
            }
        }
        return "캐치프레이즈";
    }

    private String extractProductName(String text){
        for(String line : text.split("\n")) {
            line = line.trim();
            if (line.matches(".*가장 적합한 제품:.*")) {
                return line.replaceAll(".*가장 적합한 제품:\\s*(.*)", "$1");
            }
        }
        return "상품명";
    }
}
