package com.example.TODAIT__BE.domain.taxonomy.exception;

import com.example.TODAIT__BE.domain.taxonomy.code.TaxonomyErrorCode;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;

public class TaxonomyException extends ProjectException {

    public TaxonomyException(TaxonomyErrorCode errorCode) {
        super(errorCode);
    }
}
