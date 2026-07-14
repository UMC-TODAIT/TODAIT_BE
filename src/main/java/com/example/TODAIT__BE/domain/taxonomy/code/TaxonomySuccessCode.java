package com.example.TODAIT__BE.domain.taxonomy.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TaxonomySuccessCode implements BaseSuccessCode {
    PLACE_CATEGORY_LIST_OK(HttpStatus.OK,
            "TAXONOMY200",
            "장소 카테고리 목록 조회 성공"),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
