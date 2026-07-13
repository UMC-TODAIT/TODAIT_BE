package com.example.TODAIT__BE.domain.member.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EmailVerificationErrorCode implements BaseErrorCode {
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST,
            "EMAIL400_1",
            "올바르지 않은 이메일 형식입니다."),
    CODE_MISMATCH(HttpStatus.BAD_REQUEST,
            "EMAIL400_2",
            "인증번호가 일치하지 않습니다."),
    CODE_NOT_FOUND(HttpStatus.BAD_REQUEST,
            "EMAIL400_3",
            "인증번호가 만료되었거나 존재하지 않습니다."),
    ALREADY_COMPLETED(HttpStatus.BAD_REQUEST,
            "EMAIL400_4",
            "이미 인증이 완료된 이메일입니다."),
    SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
            "EMAIL500_1",
            "인증번호 이메일 발송에 실패했습니다."),
    STORE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
            "EMAIL500_2",
            "인증번호 저장에 실패했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
