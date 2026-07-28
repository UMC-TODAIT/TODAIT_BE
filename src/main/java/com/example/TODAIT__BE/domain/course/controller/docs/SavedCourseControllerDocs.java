package com.example.TODAIT__BE.domain.course.controller.docs;

import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseOverviewResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Saved Course", description = "저장 코스 관련 API")
public interface SavedCourseControllerDocs {

    @Operation(
            summary = "저장 코스 목록 조회",
            description = "로그인한 사용자의 최근 저장 코스와 많이 이용한 코스를 조회합니다."
    )
    ResponseEntity<ApiResponse<SavedCourseOverviewResponse>>
    getSavedCourseOverview(AuthMember authMember);
}
