package com.example.TODAIT__BE.domain.recommendation.exception.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecommendationErrorCode implements BaseErrorCode {

    INVALID_PAGE(HttpStatus.BAD_REQUEST,
            "RECOMMENDATION400",
            "페이지 번호는 0 이상이어야 합니다."),
    INVALID_SIZE(HttpStatus.BAD_REQUEST,
            "RECOMMENDATION400",
            "추천 코스 조회 개수는 1개 이상 18개 이하여야 합니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
