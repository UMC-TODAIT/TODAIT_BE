package com.example.TODAIT__BE.domain.course.controller.docs;

import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftMoodTagSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftMoodTagSaveResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Course Draft", description = "임시 코스(코스 생성 플로우) 관련 API")
public interface CourseDraftMoodTagControllerDocs {

    @Operation(
            summary = "분위기 태그 선택 저장",
            description = "임시 코스에 분위기 태그 선택값을 전체 교체 저장하고, 상태를 FOOD_SELECTING으로 변경합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<CourseDraftMoodTagSaveResponse>> saveMoodTags(
            Long courseDraftId,
            AuthMember authMember,
            CourseDraftMoodTagSaveRequest request
    );
}
