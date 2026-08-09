package com.example.TODAIT__BE.domain.taxonomy.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AreaErrorCode implements BaseErrorCode {

    AREA_NOT_SUPPORTED(
            HttpStatus.BAD_REQUEST,
            "AREA400",
            "지원하지 않는 지역입니다."
    ),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
