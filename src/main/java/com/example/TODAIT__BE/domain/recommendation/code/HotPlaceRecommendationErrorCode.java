package com.example.TODAIT__BE.domain.recommendation.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum HotPlaceRecommendationErrorCode implements BaseErrorCode {

    INVALID_HOT_PLACE_SIZE(
            HttpStatus.BAD_REQUEST,
            "RECOMMENDATION400_3",
            "추천 장소 조회 개수는 1개 이상 10개 이하여야 합니다."
    ),

    INCOMPLETE_COORDINATES(
            HttpStatus.BAD_REQUEST,
            "RECOMMENDATION400_4",
            "위도와 경도는 함께 전달해야 합니다."
    ),

    INVALID_COORDINATES(
            HttpStatus.BAD_REQUEST,
            "RECOMMENDATION400_5",
            "유효하지 않은 위치 좌표입니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
