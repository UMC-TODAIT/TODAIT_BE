package com.example.TODAIT__BE.domain.member.exception;

import com.example.TODAIT__BE.domain.member.code.AuthErrorCode;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;

public class AuthException extends ProjectException {
    public AuthException(AuthErrorCode errorCode){super(errorCode);}
}
