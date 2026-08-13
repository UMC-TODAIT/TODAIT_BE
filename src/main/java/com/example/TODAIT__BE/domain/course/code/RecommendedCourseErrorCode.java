package com.example.TODAIT__BE.domain.course.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecommendedCourseErrorCode implements BaseErrorCode {

    RECOMMENDED_COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE404", "추천 코스를 찾을 수 없습니다."),
    RECOMMENDED_COURSE_NOT_SAVABLE(HttpStatus.BAD_REQUEST, "COURSE400_5", "저장할 수 없는 추천 코스입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
