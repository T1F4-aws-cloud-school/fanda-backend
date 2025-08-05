package com.fanda.feedback.service;

import com.fanda.feedback.dto.ReportResponseDto;
import com.fanda.feedback.entity.CollectedReview;
import com.fanda.feedback.repository.BedrockClient;
import com.fanda.feedback.repository.CollectedReviewRepository;
import com.fanda.feedback.template.PromptTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewAnalysisService {

    private final CollectedReviewRepository collectedReviewRepository;
    private final BedrockClient bedrockClient;

    public ReportResponseDto generateReports(){
        List<CollectedReview> reviews = collectedReviewRepository.findAll();

        String reviewText = reviews.stream().map(CollectedReview::getContent)
                .collect(Collectors.joining("\n- 리뷰: ", "\n", ""));

        String positivePrompt = PromptTemplate.getPositivePrompt(reviewText);
        String negativePrompt = PromptTemplate.getNegativePrompt(reviewText);

        String positiveReport = bedrockClient.generate(positivePrompt);
        String negativeReport = bedrockClient.generate(negativePrompt);

        return new ReportResponseDto(positiveReport, negativeReport);
    }
}
