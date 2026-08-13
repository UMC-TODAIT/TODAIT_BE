package com.example.TODAIT__BE.domain.course.code;

import com.example.TODAIT__BE.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CourseDraftErrorCode implements BaseErrorCode {

    COURSE_DRAFT_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_DRAFT404", "임시 코스를 찾을 수 없습니다."),
    COURSE_DRAFT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "COURSE403_1", "임시 코스에 대한 권한이 없습니다."),
    INVALID_MOOD_TAG_COUNT(HttpStatus.BAD_REQUEST, "COURSE_MOOD400", "분위기 태그는 중복 없이 2개 이상 6개 이하로 선택해야 합니다."),
    DUPLICATE_MOOD_TAG(HttpStatus.BAD_REQUEST, "COURSE400_2", "중복된 분위기 태그가 포함되어 있습니다."),
    MOOD_TAG_DRAFT_STATUS_CONFLICT(HttpStatus.CONFLICT, "COURSE_MOOD409", "현재 임시 코스 상태에서는 분위기를 저장할 수 없습니다."),
    INVALID_FOOD_CATEGORY_COUNT(HttpStatus.BAD_REQUEST, "COURSE400_3", "음식 카테고리는 1개 이상 선택해야 합니다."),
    DUPLICATE_FOOD_CATEGORY(HttpStatus.BAD_REQUEST, "COURSE400_4", "중복된 음식 카테고리가 포함되어 있습니다."),
    FOOD_CATEGORY_DRAFT_STATUS_CONFLICT(HttpStatus.CONFLICT, "COURSE_FOOD409", "현재 임시 코스 상태에서는 음식 카테고리를 저장할 수 없습니다."),
    INVALID_BASE_PLACE(HttpStatus.BAD_REQUEST, "COURSE_BASE400", "기준 장소가 정확히 1개 설정되어야 합니다."),
    INVALID_SELECTED_PLACE(HttpStatus.BAD_REQUEST, "COURSE_PLACE400", "선택 장소 구성이 올바르지 않습니다."),
    COURSE_DRAFT_STATUS_CONFLICT(HttpStatus.CONFLICT, "COURSE_DRAFT409", "현재 임시 코스 상태에서는 요청을 처리할 수 없습니다."),
    COURSE_DRAFT_BASE_PLACE_CONFLICT(HttpStatus.CONFLICT, "COURSE_BASE409", "기준 장소가 정확히 1개 설정되어야 합니다."),
    COURSE_DRAFT_SELECTED_PLACE_CONFLICT(HttpStatus.CONFLICT, "COURSE_PLACE409", "선택 장소 구성이 올바르지 않습니다."),
    SELECTED_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_PLACE404", "임시 코스에 속하지 않은 선택 장소가 포함되어 있습니다."),
    BASE_PLACE_NOT_REORDERABLE(HttpStatus.BAD_REQUEST, "COURSE_BASE_REORDER400", "기준 장소는 순서 변경 대상이 될 수 없습니다."),
    INVALID_VISIT_ORDER(HttpStatus.BAD_REQUEST, "COURSE_ORDER400", "방문 순서가 올바르지 않습니다."),
    PLACE_ORDER_DRAFT_STATUS_CONFLICT(HttpStatus.CONFLICT, "COURSE_ORDER409", "현재 임시 코스 상태에서는 장소 순서를 변경할 수 없습니다."),
    ORDERING_ENTRY_STATUS_CONFLICT(HttpStatus.CONFLICT, "COURSE409", "현재 임시 코스 상태에서는 순서 설정 화면으로 이동할 수 없습니다."),
    ORDERING_ENTRY_INVALID_BASE_PLACE(HttpStatus.CONFLICT, "COURSE_BASE409_1", "기준 장소 구성 정보가 올바르지 않습니다."),
    ORDERING_ENTRY_SELECTED_PLACE_REQUIRED(HttpStatus.CONFLICT, "COURSE_PLACE409_1", "순서를 설정하려면 장소를 하나 이상 추가해야 합니다."),
    ORDERING_ENTRY_INVALID_VISIT_ORDER(HttpStatus.CONFLICT, "COURSE_PLACE409_2", "장소 방문 순서 정보가 올바르지 않습니다."),
    BASE_PLACE_DRAFT_STATUS_CONFLICT(HttpStatus.CONFLICT, "COURSE_BASE409_2", "현재 임시 코스 상태에서는 기준 장소를 설정할 수 없습니다."),
    BASE_PLACE_SOURCE_CONFLICT(HttpStatus.BAD_REQUEST, "COURSE400_6", "내부 장소와 외부 장소 정보 중 하나만 전달해야 합니다."),
    BASE_PLACE_SOURCE_MISSING(HttpStatus.BAD_REQUEST, "COURSE400_7", "기준 장소 정보가 필요합니다."),
    PLACE_ADD_DRAFT_STATUS_CONFLICT(HttpStatus.CONFLICT, "COURSE_PLACE409_3", "현재 임시 코스 상태에서는 장소를 추가할 수 없습니다."),
    BASE_PLACE_RESELECT_CONFLICT(HttpStatus.CONFLICT, "COURSE_PLACE409_4", "기준 장소는 다시 추가할 수 없습니다."),
    SELECTED_PLACE_DUPLICATE(HttpStatus.CONFLICT, "COURSE_PLACE409_5", "이미 선택한 장소입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
