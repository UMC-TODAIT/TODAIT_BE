package com.example.TODAIT__BE.domain.member.exception;

import com.example.TODAIT__BE.domain.member.code.OAuthErrorCode;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;

public class OAuthException extends ProjectException {
    public OAuthException(OAuthErrorCode errorCode)
    {
        super(errorCode);
    }
}
