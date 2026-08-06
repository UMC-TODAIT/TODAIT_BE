package com.example.TODAIT__BE.domain.member.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PasswordResetErrorCode implements BaseErrorCode {

    INVALID_EMAIL_FORMAT(
            HttpStatus.BAD_REQUEST,
            "AUTH400_1",
            "올바르지 않은 이메일 형식입니다."
    ),

    EMAIL_MEMBER_ONLY(
            HttpStatus.BAD_REQUEST,
            "AUTH400_2",
            "일반 이메일 회원만 비밀번호 재설정이 가능합니다."
    ),

    EMAIL_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "AUTH404_1",
            "해당 이메일로 가입된 회원이 없습니다."
    ),

    RESEND_COOLDOWN(
            HttpStatus.TOO_MANY_REQUESTS,
            "AUTH429_1",
            "인증번호 발송 요청이 너무 많습니다."
    ),

    SEND_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "AUTH500_1",
            "비밀번호 재설정 인증번호 이메일 발송에 실패했습니다."
    ),

    STORE_FAILED(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "AUTH500_2",
            "비밀번호 재설정 인증번호 저장에 실패했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
