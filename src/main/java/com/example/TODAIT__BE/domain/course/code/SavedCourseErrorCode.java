package com.example.TODAIT__BE.domain.course.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SavedCourseErrorCode implements BaseErrorCode {

    SAVED_COURSE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "COURSE403_2", "해당 저장 코스에 접근할 권한이 없습니다."),
    SAVED_COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE404_1", "저장 코스를 찾을 수 없습니다."),
    SAVED_COURSE_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_PLACE404_1", "저장 코스의 장소를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
