package com.example.TODAIT__BE.domain.taxonomy.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FoodCategoryErrorCode implements BaseErrorCode {

    FOOD_CATEGORY_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "FOOD_CATEGORY404",
            "존재하지 않는 음식 카테고리가 포함되어 있습니다."
    ),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
