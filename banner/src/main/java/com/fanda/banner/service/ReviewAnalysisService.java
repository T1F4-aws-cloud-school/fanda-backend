package com.fanda.banner.service;

import com.fanda.banner.config.S3Uploader;
import com.fanda.banner.dto.ReportResponseDto;
import com.fanda.banner.entity.CollectedReview;
import com.fanda.banner.generator.PdfGenerator;
import com.fanda.banner.repository.BedrockClient;
import com.fanda.banner.repository.CollectedReviewRepository;
import com.fanda.banner.repository.ShopClient;
import com.fanda.banner.template.PromptTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewAnalysisService {

    private final CollectedReviewRepository collectedReviewRepository;
    private final BedrockClient bedrockClient;
    private final S3Uploader s3Uploader;
    private final ImageGenerationService imageGenerationService;
    private final ShopClient shopClient;

    public ReportResponseDto generateAndUploadPdfReports(){
        // 오늘 수집된 리뷰
        List<CollectedReview> reviews = collectedReviewRepository.findAll();

        // 긍정 리포트 용
        String positiveReviewText = reviews.stream().map(CollectedReview::getContent).collect(Collectors.joining("\n- 리뷰: ", "\n", ""));

        // 부정 리포트 용 : 평점 낮은 상품 리뷰만
        Long lowestProductId = findLowestRatedProductId(reviews);

        // 상품명 추출
        String lowestProductName = shopClient.getProductName(lowestProductId);

        List<CollectedReview> negativeReviews = reviews.stream()
                .filter(r -> r.getProductId().equals(lowestProductId)).toList();

        String negativeReviewText = negativeReviews.stream()
                .map(CollectedReview::getContent)
                .collect(Collectors.joining("\n- 리뷰: ", "\n", ""));

        // 1. 프롬프트 생성
        String positivePrompt = PromptTemplate.getPositivePrompt(positiveReviewText);
        String negativePrompt = PromptTemplate.getNegativePrompt(negativeReviewText, lowestProductName);

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
        File positivePdf = new File("positive_"+timestamp+".pdf");
        File negativePdf = new File("negative_"+timestamp+".pdf");

        try{
            PdfGenerator.saveTextAsPdf(positiveReport, positivePdf.getPath());
            PdfGenerator.saveTextAsPdf(negativeReport, negativePdf.getPath());

            String imageUrl = s3Uploader.uploadImageBytes(imageBytes, imageKey);

            s3Uploader.uploadFile(positivePdf, positiveKey);
            s3Uploader.uploadFile(negativePdf, negativeKey);

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

    // 평점 낮은 상품 ID 반환
    private Long findLowestRatedProductId(List<CollectedReview> reviews) {
        return reviews.stream()
                .filter(r -> r.getProductId() != null && r.getProductId() > 0) // 잘못된 값 방지
                .collect(Collectors.groupingBy(
                        CollectedReview::getProductId,
                        Collectors.averagingInt(CollectedReview::getRating)
                ))
                .entrySet().stream()
                .min(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalArgumentException("평점 낮은 상품 없음"));
    }

    private String extractCatchPhrase(String text) {
        boolean inTarget = false;
        for (String line : text.split("\n")) {
            line = line.trim();
            if (line.equalsIgnoreCase("[MARKETING_PHRASES]")) {
                inTarget = true;
                continue;
            }
            if (inTarget && line.startsWith("-")) {
                return line.substring(1).trim();
            }
        }
        return "[캐치프레이즈 추출 실패]";
    }


    // 상품명 추출
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
