package com.example.TODAIT__BE.domain.taxonomy.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlaceCategoryErrorCode implements BaseErrorCode {

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
