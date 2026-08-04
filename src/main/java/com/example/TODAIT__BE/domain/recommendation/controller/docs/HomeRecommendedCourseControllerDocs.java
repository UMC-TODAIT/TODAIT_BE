package com.example.TODAIT__BE.domain.recommendation.controller.docs;

import com.example.TODAIT__BE.domain.recommendation.dto.response.HomeRecommendedCourseListResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Recommendation", description = "추천 관련 API")
public interface HomeRecommendedCourseControllerDocs {

    @Operation(
            summary = "홈 화면 추천 코스 목록 조회",
            description = """
                    홈 화면 '오늘의 추천 코스' 영역에 표시할 서비스 추천 코스 목록을 조회합니다.
                    운영자가 사전 구성한 추천 코스(홍대·연남·성수) 중 현재 날짜의 지역 로테이션과
                    운영자 노출 우선순위를 기준으로 노출 코스를 결정합니다.
                    사용자 위치·취향·임시 코스·기준 장소는 사용하지 않습니다.
                    cursor가 없으면 첫 목록을 반환하며, size 기본값은 3(1~18)입니다.
                    """
    )
    ResponseEntity<ApiResponse<HomeRecommendedCourseListResponse>>
    getHomeRecommendedCourses(
            AuthMember authMember,
            @Parameter(description = "다음 목록 조회 커서")
            String cursor,
            @Parameter(description = "페이지 크기 (1~18, 기본값 3)") Integer size
    );
}
