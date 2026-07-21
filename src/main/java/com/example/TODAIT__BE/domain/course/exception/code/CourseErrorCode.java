package com.example.TODAIT__BE.domain.course.exception.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CourseErrorCode implements BaseErrorCode {

    COURSE_DRAFT_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_DRAFT404", "임시 코스를 찾을 수 없습니다."),
    NOT_COURSE_DRAFT_OWNER(HttpStatus.FORBIDDEN, "AUTH403", "본인의 임시 코스만 수정할 수 있습니다."),
    INVALID_COURSE_DRAFT_STATUS(HttpStatus.BAD_REQUEST, "COURSE_DRAFT400", "수정할 수 없는 임시 코스 상태입니다."),
    SELECTED_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_PLACE404", "임시 코스에 속하지 않은 선택 장소가 포함되어 있습니다."),
    BASE_PLACE_NOT_REORDERABLE(HttpStatus.BAD_REQUEST, "COURSE_BASE400", "기준 장소는 순서 변경 대상이 될 수 없습니다."),
    INVALID_VISIT_ORDER(HttpStatus.BAD_REQUEST, "COURSE_ORDER400", "방문 순서가 올바르지 않습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
