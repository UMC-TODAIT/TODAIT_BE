package com.example.TODAIT__BE.domain.course.controller.docs;

import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseOverviewResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseDetailResponse;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "COURSE", description = "임시 코스 및 저장 코스 API")
public interface SavedCourseControllerDocs {

    @Operation(
            summary = "[저장 코스] 저장 코스 목록 조회",
            description = "로그인한 사용자의 최근 저장 코스와 조회수 기준 인기 코스를 조회합니다."
    )
    ResponseEntity<ApiResponse<SavedCourseOverviewResponse>>
    getSavedCourseOverview(AuthMember authMember);

    @Operation(
            summary = "[저장 코스] 저장 코스 상세 조회",
            description = "로그인한 사용자가 소유한 저장 코스의 상세 정보를 조회합니다."
    )
    ResponseEntity<ApiResponse<SavedCourseDetailResponse>>
    getSavedCourseDetail(
            AuthMember authMember,
            @PathVariable Long courseId
    );
}
