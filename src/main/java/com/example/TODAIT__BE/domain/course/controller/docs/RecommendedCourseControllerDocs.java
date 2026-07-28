package com.example.TODAIT__BE.domain.course.controller.docs;

import com.example.TODAIT__BE.domain.course.dto.response.RecommendedCourseDetailResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Recommended Course", description = "추천 코스 관련 API")
public interface RecommendedCourseControllerDocs {

    @Operation(
            summary = "추천 코스 상세 조회",
            description = "추천 코스 ID를 기준으로 코스 기본 정보와 방문 장소 목록을 조회합니다."
    )
    ResponseEntity<ApiResponse<RecommendedCourseDetailResponse>>
    getRecommendedCourseDetail(Long courseId);
}
