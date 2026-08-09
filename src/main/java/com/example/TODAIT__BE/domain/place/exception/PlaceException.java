package com.example.TODAIT__BE.domain.place.exception;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;

public class PlaceException extends ProjectException {

    public PlaceException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
