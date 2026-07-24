package com.example.TODAIT__BE.domain.member.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OAuthSuccessCode implements BaseSuccessCode {
    OAUTH_LOGIN_OK(
            HttpStatus.OK,
            "AUTH200_1",
            "소셜 로그인 성공"
    ),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
