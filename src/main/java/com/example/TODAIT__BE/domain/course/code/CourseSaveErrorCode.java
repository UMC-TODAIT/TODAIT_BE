package com.example.TODAIT__BE.domain.course.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CourseSaveErrorCode implements BaseErrorCode {

    COURSE_DRAFT_ALREADY_COMPLETED(HttpStatus.CONFLICT, "COURSE_DRAFT_COMPLETED409", "이미 저장이 완료된 임시 코스입니다."),
    INVALID_COURSE_TITLE(HttpStatus.BAD_REQUEST, "COURSE_TITLE400", "코스 제목을 입력해주세요."),
    FOOD_CATEGORY_NOT_SELECTED(HttpStatus.BAD_REQUEST, "COURSE_FOOD400", "임시 코스에 선택된 음식 카테고리가 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
