package com.example.TODAIT__BE.domain.course.controller.docs;

import com.example.TODAIT__BE.domain.course.dto.response.RecommendedCourseDetailResponse;
import com.example.TODAIT__BE.domain.course.dto.response.RecommendedCourseSaveResponse;
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
            summary = "[추천 코스] 추천 코스 상세 조회",
            description = """
                    추천 코스 ID를 기준으로 코스 기본 정보와
                    방문 장소 목록을 조회합니다.
                    """
    )
    ResponseEntity<ApiResponse<RecommendedCourseDetailResponse>>
    getRecommendedCourseDetail(Long courseId);

    @Operation(
            summary = "[추천 코스] 추천 코스 저장",
            description = """
                    서비스 추천 코스를 로그인 사용자의 개인 코스로 복사합니다.

                    원본 추천 코스의 기본 정보, 장소, 방문 순서,
                    분위기 태그와 음식 카테고리를 복사합니다.

                    동일한 추천 코스를 여러 번 저장할 수 있으며,
                    요청할 때마다 새로운 사용자 코스가 생성됩니다.
                    """
    )
    ResponseEntity<ApiResponse<RecommendedCourseSaveResponse>>
    saveRecommendedCourse(
            Long courseId,
            AuthMember authMember
    );
}
