package com.example.TODAIT__BE.domain.course.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CourseSuccessCode implements BaseSuccessCode {
    COURSE_DRAFT_CREATE_OK(HttpStatus.CREATED,
            "COURSE201",
            "임시 코스 생성 성공"),
    COURSE_SAVE_OK(HttpStatus.CREATED,
            "COURSE_SAVE201",
            "코스 저장 성공"),
    PLACE_ORDER_UPDATE_OK(HttpStatus.OK,
            "COURSE200",
            "코스 장소 순서 변경 성공"),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
