package com.example.TODAIT__BE.domain.member.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    INVALID_REFRESH_TOKEN(
            HttpStatus.UNAUTHORIZED,
            "AUTH401_1",
            "유효하지 않은 Refresh Token입니다."
    ),

    REVOKED_REFRESH_TOKEN(
            HttpStatus.FORBIDDEN,
            "AUTH403_1",
            "폐기된 Refresh Token입니다."
    ),

    EXPIRED_REFRESH_TOKEN(
            HttpStatus.GONE,
            "AUTH410_1",
            "만료된 Refresh Token입니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
