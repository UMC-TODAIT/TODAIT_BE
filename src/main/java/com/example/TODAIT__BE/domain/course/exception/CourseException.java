package com.example.TODAIT__BE.domain.course.exception;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;

public class CourseException extends ProjectException {

    public CourseException(BaseErrorCode errorCode) {
        super(errorCode);
    }

    public CourseException(BaseErrorCode errorCode, Object result) {
        super(errorCode, result);
    }
}
