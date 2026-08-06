package com.example.TODAIT__BE.domain.recommendation.controller.docs;

import com.example.TODAIT__BE.domain.recommendation.dto.response.HomeRecommendedCourseListResponse;
import com.example.TODAIT__BE.domain.recommendation.dto.response.HomeRecommendedPlaceListResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "RECOMMENDATION", description = "추천 도메인 API")
public interface HomeRecommendationControllerDocs {

    @Operation(
            summary = "[홈] 추천 코스 목록 조회",
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
            @Parameter(description = "페이지 크기 (1~18, 기본값 3)")
            Integer size
    );

    @Operation(
            summary = "[홈] 추천 장소 목록 조회",
            description = """
                    홈 화면의 추천 장소 영역에 표시할 운영자 관리 장소 목록을 조회합니다.

                    위치정보가 있으면 500m 이내 여부, 거리, 운영자 우선순위를 기준으로 정렬합니다.
                    위치정보가 없으면 홍대·성수·연남 지역별 라운드 로빈 방식으로 결과를 구성합니다.

                    cursor가 없으면 첫 목록을 반환하며, size 기본값은 2(1~20)입니다.
                    latitude와 longitude는 반드시 함께 전달해야 합니다.
                    """
    )
    ResponseEntity<ApiResponse<HomeRecommendedPlaceListResponse>>
    getHomeRecommendedPlaces(
            AuthMember authMember,
            @Parameter(description = "다음 목록 조회 커서")
            String cursor,
            @Parameter(description = "페이지 크기 (1~20, 기본값 2)")
            Integer size,
            @Parameter(description = "사용자 현재 위도 (-90~90)")
            Double latitude,
            @Parameter(description = "사용자 현재 경도 (-180~180)")
            Double longitude
    );
}
