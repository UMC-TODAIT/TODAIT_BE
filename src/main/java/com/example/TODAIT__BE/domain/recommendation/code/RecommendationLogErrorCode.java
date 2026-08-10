package com.example.TODAIT__BE.domain.recommendation.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecommendationLogErrorCode implements BaseErrorCode {

    REQUEST_CONTEXT_SERIALIZATION_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "RECOMMENDATION500_1",
            "추천 요청 정보를 처리하는 중 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
