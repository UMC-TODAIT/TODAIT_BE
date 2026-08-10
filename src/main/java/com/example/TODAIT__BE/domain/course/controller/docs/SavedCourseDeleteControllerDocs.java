package com.example.TODAIT__BE.domain.course.controller.docs;

import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseDeleteResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(
        name = "COURSE",
        description = "코스 생성, 저장 및 조회 API"
)
public interface SavedCourseDeleteControllerDocs {

    @Operation(
            summary = "[저장 코스] 저장 코스 삭제",
            description = """
                    현재 로그인한 사용자가 소유한 저장 코스를 삭제합니다.

                    - 삭제 방식: course.deletedAt 기반 소프트 삭제
                    - USER_CREATED 코스만 삭제할 수 있습니다.
                    - SERVICE_CREATED 추천 원본 코스는 삭제 대상이 아닙니다.
                    - 삭제된 코스는 저장 코스 목록 및 상세 조회 대상에서 제외됩니다.
                    - course_place, course_mood_tag, course_food_category 등의 연관 데이터는 물리 삭제하지 않습니다.
                    - 이미 삭제된 코스 또는 존재하지 않는 코스는 SAVED_COURSE_NOT_FOUND로 처리합니다.
                    - 다른 사용자의 저장 코스는 SAVED_COURSE_ACCESS_DENIED로 처리합니다.
                    """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<SavedCourseDeleteResponse>>
    deleteSavedCourse(
            @Parameter(hidden = true)
            AuthMember authMember,

            @Parameter(
                    description = "삭제할 저장 코스 ID",
                    example = "10",
                    required = true
            )
            @PathVariable Long courseId
    );
}
