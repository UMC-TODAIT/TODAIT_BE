package com.example.TODAIT__BE.domain.recommendation.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecommendedPlaceErrorCode implements BaseErrorCode {

    INVALID_PLACE_SIZE(
            HttpStatus.BAD_REQUEST,
            "RECOMMENDATION400_6",
            "추천 장소 조회 개수는 1개 이상 20개 이하여야 합니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
