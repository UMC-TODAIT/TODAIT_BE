package com.example.TODAIT__BE.domain.course.controller.docs;

import com.example.TODAIT__BE.domain.course.dto.response.RecommendedCourseResponse.DetailResponse;
import com.example.TODAIT__BE.domain.course.dto.response.RecommendedCourseResponse.SaveResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "COURSE",
        description = "임시 코스 및 저장 코스 API"
)
public interface RecommendedCourseControllerDocs {

    @Operation(
            summary = "[추천 코스] 서비스 추천 코스 상세 조회",
            description = """
                    서비스 추천 코스 ID를 기준으로 상세 정보를 조회합니다.

                    - 코스 기본 정보
                    - 대표 분위기/장소 카테고리
                    - 방문 장소 목록
                    """
    )
    ResponseEntity<ApiResponse<DetailResponse>>
    getRecommendedCourseDetail(Long courseId);

    @Operation(
            summary = "[추천 코스] 추천 코스를 내 코스로 저장",
            description = """
                    서비스 추천 코스를 로그인 사용자의 개인 코스로 복사합니다.

                    - 복사 대상: 기본 정보, 장소, 방문 순서, 분위기 태그, 음식 카테고리
                    - 저장 결과: PRIVATE 사용자 코스
                    - 중복 저장: 같은 추천 코스를 여러 번 저장 가능
                    """
    )
    ResponseEntity<ApiResponse<SaveResponse>>
    saveRecommendedCourse(
            Long courseId,
            AuthMember authMember
    );
}
