package com.example.TODAIT__BE.domain.member.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthSuccessCode implements BaseSuccessCode {
    TOKEN_REFRESHED(
            HttpStatus.OK,
            "AUTH200_1",
            "Access Token이 재발급되었습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
