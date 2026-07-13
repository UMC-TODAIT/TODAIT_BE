package com.example.TODAIT__BE.domain.member.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EmailVerificationSuccessCode implements BaseSuccessCode {
    CODE_SENT(HttpStatus.OK,
            "EMAIL200_1",
            "인증번호가 이메일로 발송되었습니다."),
    COMPLETED(HttpStatus.OK,
            "EMAIL200_2",
            "이메일 인증이 완료되었습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
