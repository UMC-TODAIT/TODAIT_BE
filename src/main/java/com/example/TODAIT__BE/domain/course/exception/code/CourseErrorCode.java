package com.example.TODAIT__BE.domain.course.exception.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CourseErrorCode implements BaseErrorCode {

    COURSE_DRAFT_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_DRAFT404", "임시 코스를 찾을 수 없습니다."),
    NOT_COURSE_DRAFT_OWNER(HttpStatus.FORBIDDEN, "AUTH403", "본인의 임시 코스만 저장할 수 있습니다."),
    COURSE_DRAFT_ALREADY_COMPLETED(HttpStatus.CONFLICT, "COURSE_DRAFT409", "이미 저장이 완료된 임시 코스입니다."),
    INVALID_COURSE_TITLE(HttpStatus.BAD_REQUEST, "COURSE_TITLE400", "코스 제목을 입력해주세요."),
    INVALID_MOOD_TAG_COUNT(HttpStatus.BAD_REQUEST, "COURSE_MOOD400", "분위기 태그는 중복 없이 1개 이상 6개 이하로 선택해야 합니다."),
    MOOD_TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_MOOD404", "존재하지 않는 분위기 태그가 포함되어 있습니다."),
    FOOD_CATEGORY_NOT_SELECTED(HttpStatus.BAD_REQUEST, "COURSE_FOOD400", "임시 코스에 선택된 음식 카테고리가 없습니다."),
    INVALID_BASE_PLACE(HttpStatus.BAD_REQUEST, "COURSE_BASE400", "기준 장소가 정확히 1개 설정되어야 합니다."),
    INVALID_SELECTED_PLACE(HttpStatus.BAD_REQUEST, "COURSE_PLACE400", "선택 장소 구성이 올바르지 않습니다."),
    INVALID_COURSE_DRAFT_STATUS(HttpStatus.BAD_REQUEST, "COURSE_DRAFT400", "수정할 수 없는 임시 코스 상태입니다."),
    SELECTED_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_PLACE404", "임시 코스에 속하지 않은 선택 장소가 포함되어 있습니다."),
    BASE_PLACE_NOT_REORDERABLE(HttpStatus.BAD_REQUEST, "COURSE_BASE_REORDER400", "기준 장소는 순서 변경 대상이 될 수 없습니다."),
    INVALID_VISIT_ORDER(HttpStatus.BAD_REQUEST, "COURSE_ORDER400", "방문 순서가 올바르지 않습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
