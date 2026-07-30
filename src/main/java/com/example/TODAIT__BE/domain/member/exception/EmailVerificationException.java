package com.example.TODAIT__BE.domain.member.exception;

import com.example.TODAIT__BE.domain.member.code.EmailVerificationErrorCode;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;

public class EmailVerificationException extends ProjectException {

    public EmailVerificationException(EmailVerificationErrorCode errorCode) {
        super(errorCode);
    }

    public EmailVerificationException(EmailVerificationErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
