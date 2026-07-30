package com.example.TODAIT__BE.domain.place.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlaceErrorCode implements BaseErrorCode {

    INVALID_SEARCH_KEYWORD(
            HttpStatus.BAD_REQUEST,
            "PLACE400_1",
            "검색어를 입력해주세요."
    ),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
