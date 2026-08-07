package com.example.TODAIT__BE.domain.taxonomy.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TaxonomyErrorCode implements BaseErrorCode {

    MOOD_TAG_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "MOOD_TAG404",
            "존재하지 않는 분위기 태그가 포함되어 있습니다."
    ),

    FOOD_CATEGORY_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "FOOD_CATEGORY404",
            "존재하지 않는 음식 카테고리가 포함되어 있습니다."
    ),

    AREA_NOT_SUPPORTED(
            HttpStatus.BAD_REQUEST,
            "AREA400",
            "지원하지 않는 지역입니다."
    ),

    PLACE_CATEGORY_NOT_SUPPORTED(
            HttpStatus.BAD_REQUEST,
            "PLACE_CATEGORY400",
            "지원하지 않는 장소 카테고리입니다."
    ),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
