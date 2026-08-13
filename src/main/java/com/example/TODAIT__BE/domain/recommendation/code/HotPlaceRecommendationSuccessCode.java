package com.example.TODAIT__BE.domain.recommendation.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum HotPlaceRecommendationSuccessCode implements BaseSuccessCode {

    HOT_PLACE_LIST_OK(
            HttpStatus.OK,
            "RECOMMENDATION200_2",
            "지금 내 주변 핫플 조회 성공"
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
