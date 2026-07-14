package com.example.TODAIT__BE.domain.member.exeption;

import com.example.TODAIT__BE.domain.member.exeption.code.MemberErrorCode;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;

public class MemberException extends ProjectException {

    public MemberException(MemberErrorCode errorCode) {
        super(errorCode);
    }
}
