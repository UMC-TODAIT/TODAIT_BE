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
            "임시 코스 분위기 선택 저장 성공"),

    FOOD_CATEGORY_SAVE_OK(HttpStatus.OK,
            "COURSE200_2",
            "임시 코스 음식 선택 저장 성공"),

    COURSE_SAVE_OK(HttpStatus.CREATED,
            "COURSE_SAVE201",
            "코스 저장 성공"),

    PLACE_ORDER_UPDATE_OK(HttpStatus.OK,
            "COURSE200",
            "임시 코스 장소 순서 변경 성공"),

    ORDERING_ENTRY_OK(HttpStatus.OK,
            "COURSE200",
            "임시 코스 순서 설정 화면 진입 성공"),

    RECOMMENDED_COURSE_DETAIL_OK(HttpStatus.OK,
            "COURSE200_3",
            "추천 코스 상세 조회 성공"),

    SAVED_COURSE_OVERVIEW_OK(
            HttpStatus.OK,
            "COURSE200_4",
            "저장 코스 목록 조회 성공"
    ),

    SAVED_COURSE_DETAIL_OK(
            HttpStatus.OK,
            "COURSE200_5",
            "저장 코스 상세 조회 성공"
    ),

    RECOMMENDED_COURSE_SAVE_OK(
            HttpStatus.CREATED,
            "COURSE202",
            "추천 코스 저장 성공"
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
