package com.example.TODAIT__BE.domain.course.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SavedCourseSuccessCode implements BaseSuccessCode {

    SAVED_COURSE_OVERVIEW_OK(HttpStatus.OK, "COURSE200_4", "저장 코스 목록 조회 성공"),
    SAVED_COURSE_DETAIL_OK(HttpStatus.OK, "COURSE200_5", "저장 코스 상세 조회 성공"),
    SAVED_COURSE_DELETE_OK(HttpStatus.OK, "COURSE200_7", "저장 코스 삭제 성공"),
    SAVED_COURSE_MEMO_UPDATE_OK(HttpStatus.OK, "COURSE200_10", "저장 코스 메모 수정 성공"),
    SAVED_COURSE_PLACE_MEMO_UPDATE_OK(HttpStatus.OK, "COURSE200_11", "저장 코스 장소 메모 수정 성공");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
