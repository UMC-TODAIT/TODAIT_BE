package com.example.TODAIT__BE.domain.taxonomy.exception;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;

public class TaxonomyException extends ProjectException {

    public TaxonomyException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
