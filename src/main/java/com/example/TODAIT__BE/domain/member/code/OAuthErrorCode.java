package com.example.TODAIT__BE.domain.member.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OAuthErrorCode implements BaseErrorCode {
    INVALID_GOOGLE_ID_TOKEN(
            HttpStatus.BAD_REQUEST,
            "AUTH400_1",
            "유효하지 않은 구글 idToken입니다."
    ),

    GOOGLE_ID_TOKEN_VERIFICATION_FAILED(
            HttpStatus.BAD_GATEWAY,
            "AUTH502_1",
            "구글 idToken 검증에 실패했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
