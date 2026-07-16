package com.example.TODAIT__BE.domain.member.exception.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {

    ALREADY_REGISTERED_EMAIL(
            HttpStatus.CONFLICT,
            "MEMBER409_1",
            "이미 가입된 이메일입니다."
    ),

    INVALID_MEMBER_STATUS(
            HttpStatus.FORBIDDEN,
            "MEMBER403_1",
            "로그인할 수 없는 회원 상태입니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
