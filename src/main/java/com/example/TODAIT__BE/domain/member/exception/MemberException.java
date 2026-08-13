package com.example.TODAIT__BE.domain.member.exception;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;

public class MemberException extends ProjectException {

    public MemberException(BaseErrorCode errorCode) {
        super(errorCode);
    }

    public MemberException(BaseErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
