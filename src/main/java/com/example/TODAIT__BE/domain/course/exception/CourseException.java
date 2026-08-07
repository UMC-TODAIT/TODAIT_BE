package com.example.TODAIT__BE.domain.course.exception;

import com.example.TODAIT__BE.domain.course.exception.code.CourseErrorCode;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;

public class CourseException extends ProjectException {

    public CourseException(CourseErrorCode errorCode) {
        super(errorCode);
    }

    public CourseException(CourseErrorCode errorCode, Object result) {
        super(errorCode, result);
    }
}
