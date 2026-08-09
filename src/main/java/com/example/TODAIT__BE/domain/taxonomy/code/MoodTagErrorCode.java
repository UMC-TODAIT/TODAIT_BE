package com.example.TODAIT__BE.domain.taxonomy.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MoodTagErrorCode implements BaseErrorCode {

    MOOD_TAG_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "MOOD_TAG404",
            "존재하지 않는 분위기 태그가 포함되어 있습니다."
    ),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
