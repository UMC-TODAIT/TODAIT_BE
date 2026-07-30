package com.example.TODAIT__BE.domain.place.exception;

import com.example.TODAIT__BE.domain.place.code.PlaceErrorCode;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;

public class PlaceException extends ProjectException {

    public PlaceException(PlaceErrorCode errorCode) {
        super(errorCode);
    }
}
