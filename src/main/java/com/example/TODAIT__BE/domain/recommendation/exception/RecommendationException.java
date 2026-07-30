package com.example.TODAIT__BE.domain.recommendation.exception;

import com.example.TODAIT__BE.domain.recommendation.exception.code.RecommendationErrorCode;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;

public class RecommendationException extends ProjectException {

    public RecommendationException(RecommendationErrorCode errorCode) {
        super(errorCode);
    }
}
