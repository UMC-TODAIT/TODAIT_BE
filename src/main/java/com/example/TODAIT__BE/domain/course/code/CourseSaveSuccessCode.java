package com.example.TODAIT__BE.domain.course.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CourseSaveSuccessCode implements BaseSuccessCode {

    COURSE_SAVE_OK(HttpStatus.CREATED, "COURSE_SAVE201", "코스 저장 성공");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
