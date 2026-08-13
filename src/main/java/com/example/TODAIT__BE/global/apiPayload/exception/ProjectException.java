package com.example.TODAIT__BE.global.apiPayload.exception;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;

@Getter
public class ProjectException extends RuntimeException {
    private final BaseErrorCode errorCode;
    private final Object result;

    public ProjectException(BaseErrorCode errorCode) {
        this.errorCode = errorCode;
        this.result = null;
    }

    public ProjectException(BaseErrorCode errorCode, Object result) {
        this.errorCode = errorCode;
        this.result = result;
    }

    public ProjectException(BaseErrorCode errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
        this.result = null;
    }
}
