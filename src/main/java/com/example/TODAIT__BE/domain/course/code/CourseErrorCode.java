package com.example.TODAIT__BE.domain.course.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CourseErrorCode implements BaseErrorCode {

    COURSE_DRAFT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "COURSE404_1",
            "임시 코스를 찾을 수 없습니다."
    ),

    COURSE_DRAFT_ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "COURSE403_1",
            "임시 코스에 대한 권한이 없습니다."
    ),

    INVALID_MOOD_TAG_COUNT(
            HttpStatus.BAD_REQUEST,
            "COURSE400_1",
            "분위기 태그는 최소 2개 이상 선택해야 합니다."
    ),

    DUPLICATE_MOOD_TAG(
            HttpStatus.BAD_REQUEST,
            "COURSE400_2",
            "중복된 분위기 태그가 포함되어 있습니다."
    ),

    INVALID_FOOD_CATEGORY_COUNT(
            HttpStatus.BAD_REQUEST,
            "COURSE400_3",
            "음식 카테고리는 최소 1개 이상 선택해야 합니다."
    ),

    DUPLICATE_FOOD_CATEGORY(
            HttpStatus.BAD_REQUEST,
            "COURSE400_4",
            "중복된 음식 카테고리가 포함되어 있습니다."
    ),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
