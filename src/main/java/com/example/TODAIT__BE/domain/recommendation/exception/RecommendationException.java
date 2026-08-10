package com.example.TODAIT__BE.domain.recommendation.exception;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;

public class RecommendationException extends ProjectException {

    public RecommendationException(BaseErrorCode errorCode) {
        super(errorCode);
    }

    public RecommendationException(
            BaseErrorCode errorCode,
            Throwable cause
    ) {
        super(errorCode, cause);
    }
}
