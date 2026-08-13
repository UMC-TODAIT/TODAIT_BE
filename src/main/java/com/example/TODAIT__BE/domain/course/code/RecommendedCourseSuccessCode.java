package com.example.TODAIT__BE.domain.course.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RecommendedCourseSuccessCode implements BaseSuccessCode {

    RECOMMENDED_COURSE_DETAIL_OK(HttpStatus.OK, "COURSE200_3", "추천 코스 상세 조회 성공"),
    RECOMMENDED_COURSE_SAVE_OK(HttpStatus.CREATED, "COURSE202", "추천 코스 저장 성공");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
