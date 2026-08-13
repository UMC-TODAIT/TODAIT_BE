package com.example.TODAIT__BE.domain.recommendation.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CategoryRecommendedPlaceSuccessCode implements BaseSuccessCode {

    CATEGORY_RECOMMENDED_PLACE_LIST_OK(
            HttpStatus.OK,
            "RECOMMENDATION202",
            "카테고리별 추천 장소 조회 성공"
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
