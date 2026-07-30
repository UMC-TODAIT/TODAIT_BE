package com.example.TODAIT__BE.domain.recommendation.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecommendationSuccessCode implements BaseSuccessCode {

    HOME_RECOMMENDED_COURSE_LIST_OK(HttpStatus.OK,
            "RECOMMENDATION200",
            "홈 화면 추천 코스 목록 조회 성공"),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
