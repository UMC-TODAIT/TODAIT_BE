package com.example.TODAIT__BE.domain.course.controller.docs;

import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftMoodTagSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftMoodTagSaveResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Course Draft", description = "임시 코스(코스 생성 플로우) 관련 API")
public interface CourseDraftMoodTagControllerDocs {

    @Operation(
            summary = "분위기 태그 선택 저장",
            description = "임시 코스에 분위기 태그 2개 이상 6개 이하의 선택값 전체를 PUT 방식으로 교체 저장합니다. "
                    + "MOOD_SELECTING 상태에서 최초 저장 시 FOOD_SELECTING으로 전이하며, "
                    + "FOOD_SELECTING 상태에서는 재호출 시 태그만 교체하고 상태는 유지합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<CourseDraftMoodTagSaveResponse>> saveMoodTags(
            @PathVariable Long courseDraftId,
            AuthMember authMember,
            CourseDraftMoodTagSaveRequest request
    );
}
