package com.example.TODAIT__BE.domain.course.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CourseDraftSuccessCode implements BaseSuccessCode {

    COURSE_DRAFT_CREATE_OK(HttpStatus.CREATED, "COURSE201", "임시 코스 생성 성공"),
    MOOD_TAG_SAVE_OK(HttpStatus.OK, "COURSE200_1", "임시 코스 분위기 선택 저장 성공"),
    FOOD_CATEGORY_SAVE_OK(HttpStatus.OK, "COURSE200_2", "임시 코스 음식 선택 저장 성공"),
    PLACE_ORDER_UPDATE_OK(HttpStatus.OK, "COURSE200", "임시 코스 장소 순서 변경 성공"),
    ORDERING_ENTRY_OK(HttpStatus.OK, "COURSE200_9", "임시 코스 순서 설정 화면 진입 성공"),
    COURSE_DRAFT_SAVING_ENTER_OK(HttpStatus.OK, "COURSE200_8", "임시 코스 저장 화면 진입 성공"),
    BASE_PLACE_SAVE_OK(HttpStatus.OK, "COURSE200_6", "임시 코스 기준 장소 설정 성공"),
    PLACE_ADD_OK(HttpStatus.CREATED, "COURSE201_1", "선택 장소 추가 성공"),
    COURSE_DRAFT_STATUS_UPDATE_OK(HttpStatus.OK, "COURSE200_12", "임시 코스 단계 이동 성공"),
    COURSE_DRAFT_CURRENT_OK(HttpStatus.OK, "COURSE200_14", "진행 중인 임시 코스 조회 성공"),
    COURSE_DRAFT_ABANDON_OK(HttpStatus.OK, "COURSE200_13", "임시 코스 포기 성공");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
