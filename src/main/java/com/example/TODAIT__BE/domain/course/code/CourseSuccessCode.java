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

    MOOD_TAG_SAVE_OK(HttpStatus.OK,
            "COURSE200_1",
            "임시 코스 분위기 태그 저장 성공"),

    FOOD_CATEGORY_SAVE_OK(HttpStatus.OK,
            "COURSE200_2",
            "임시 코스 음식 카테고리 저장 성공"),

    COURSE_SAVE_OK(HttpStatus.CREATED,
            "COURSE_SAVE201",
            "코스 저장 성공"),

    PLACE_ORDER_UPDATE_OK(HttpStatus.OK,
            "COURSE200",
            "임시 코스 장소 순서 변경 성공"),

    RECOMMENDED_COURSE_DETAIL_OK(HttpStatus.OK,
            "COURSE200",
            "추천 코스 상세 조회 성공"),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
