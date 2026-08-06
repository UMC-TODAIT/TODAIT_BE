package com.example.TODAIT__BE.domain.member.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PasswordResetSuccessCode implements BaseSuccessCode {

    CODE_SENT(
            HttpStatus.OK,
            "AUTH200_4",
            "비밀번호 재설정 인증번호 발송 성공"
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
