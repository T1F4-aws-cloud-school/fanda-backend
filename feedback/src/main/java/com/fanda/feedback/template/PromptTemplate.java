package com.fanda.feedback.template;

public class PromptTemplate {

    public static String getComparePrompt(String baselineReport, String improvedReviewListText){
        return """
            당신은 제품개발/마케팅 공용 보고서를 작성하는 리뷰 분석가입니다.
            아래의 '개선 전 리포트'와 '개선 후 리뷰 목록'을 바탕으로
            '개선 전 vs 개선 후' 비교 분석 보고서를 **Markdown** 형식으로 작성하세요.

            섹션 구조(제목은 그대로 유지):
            # Executive Summary
            - 핵심 개선 효과/성과를 3~5줄로 요약

            # 초기 문제 요약(개선 전)
            - 개선 전 리포트에서 제기된 문제를 목록화

            # 개선 후 정량 지표(근사치 허용)
            - 평균/중앙값 평점 변화
            - 부정 키워드 빈도 변화
            - 긍정/부정 후기 비율 변화(대략적인 수치 OK)

            # 개선 후 정성 분석
            - 주로 언급된 긍정/부정 포인트
            - 이전 대비 달라진 표현/감정 변화

            # 남아있는 이슈 & 다음 액션
            - 해결되지 않은 문제와 추가 개선 제안

            # 결론
            - 종합 평가와 추후 모니터링 포인트

            --- 
            [개선 전 리포트(원문)]
            %s

            [개선 후 리뷰(목록)]
            %s
            """.formatted(baselineReport, improvedReviewListText);
    }
}
