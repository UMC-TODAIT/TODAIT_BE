package com.example.TODAIT__BE.domain.course.controller.docs;

import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseOverviewResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseDetailResponse;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.TODAIT__BE.domain.course.dto.request.SavedCourseMemoUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.request.SavedCoursePlaceMemoUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseMemoUpdateResponse;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCoursePlaceMemoUpdateResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "COURSE", description = "임시 코스 및 저장 코스 API")
public interface SavedCourseControllerDocs {

    @Operation(
            summary = "[저장 코스] 내 저장 코스 개요 조회",
            description = """
                    로그인한 사용자의 저장 코스 개요를 조회합니다.

                    - 최근 저장 코스
                    - 조회수 기준 인기 코스
                    """
    )
    ResponseEntity<ApiResponse<SavedCourseOverviewResponse>>
    getSavedCourseOverview(AuthMember authMember);

    @Operation(
            summary = "[저장 코스] 내 저장 코스 상세 조회",
            description = """
                    로그인한 사용자가 소유한 저장 코스의 상세 정보를 조회합니다.

                    소유자가 아닌 코스는 조회할 수 없습니다.
                    """
    )
    ResponseEntity<ApiResponse<SavedCourseDetailResponse>>
    getSavedCourseDetail(
            AuthMember authMember,
            @PathVariable Long courseId
    );

    @Operation(
            summary = "[저장 코스] 코스 메모 수정",
            description = """
                로그인한 사용자가 자신이 소유한 저장 코스의 메모를 수정합니다.

                - USER_CREATED 코스만 수정할 수 있습니다.
                - null, 빈 문자열, 공백 문자열은 메모 삭제로 처리합니다.
                - 메모 앞뒤 공백은 제거하여 저장합니다.
                """,
            security = @SecurityRequirement(name = "JWT TOKEN")
    )
    ResponseEntity<ApiResponse<SavedCourseMemoUpdateResponse>>
    updateSavedCourseMemo(
            @Parameter(hidden = true)
            AuthMember authMember,

            @Parameter(
                    description = "메모를 수정할 저장 코스 ID",
                    example = "10",
                    required = true
            )
            @PathVariable Long courseId,

            @RequestBody
            SavedCourseMemoUpdateRequest request
    );

    @Operation(
            summary = "[저장 코스] 장소 메모 수정",
            description = """
                로그인한 사용자가 자신이 소유한 저장 코스에 포함된 장소의 메모를 수정합니다.

                - USER_CREATED 코스만 수정할 수 있습니다.
                - coursePlaceId는 course_place.id를 의미합니다.
                - coursePlaceId가 해당 courseId에 속하는지 검증합니다.
                - null, 빈 문자열, 공백 문자열은 메모 삭제로 처리합니다.
                - 메모 앞뒤 공백은 제거하여 저장합니다.
                """,
            security = @SecurityRequirement(name = "JWT TOKEN")
    )
    ResponseEntity<ApiResponse<SavedCoursePlaceMemoUpdateResponse>>
    updateSavedCoursePlaceMemo(
            @Parameter(hidden = true)
            AuthMember authMember,

            @Parameter(
                    description = "저장 코스 ID",
                    example = "10",
                    required = true
            )
            @PathVariable Long courseId,

            @Parameter(
                    description = "저장 코스 장소 항목 ID",
                    example = "25",
                    required = true
            )
            @PathVariable Long coursePlaceId,

            @RequestBody
            SavedCoursePlaceMemoUpdateRequest request
    );
}
