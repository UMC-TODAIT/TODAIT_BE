package com.example.TODAIT__BE.domain.recommendation.controller.docs;

import com.example.TODAIT__BE.domain.recommendation.dto.response.HomeRecommendedPlaceListResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Recommendation", description = "추천 관련 API")
public interface HomeRecommendedPlaceControllerDocs {

    @Operation(
            summary = "홈 화면 추천 장소 목록 조회",
            description = """
                    홈 화면의 추천 장소 영역에 표시할 운영자 관리 장소 목록을 조회합니다.

                    위치정보가 있으면 500m 이내 여부, 거리, 운영자 우선순위를 기준으로 정렬합니다.
                    위치정보가 없으면 홍대·성수·연남 지역별 라운드 로빈 방식으로 결과를 구성합니다.

                    page 기본값은 0, size 기본값은 2이며 size는 1~20입니다.
                    latitude와 longitude는 반드시 함께 전달해야 합니다.
                    """
    )
    ResponseEntity<ApiResponse<HomeRecommendedPlaceListResponse>>
    getHomeRecommendedPlaces(
            AuthMember authMember,
            @Parameter(description = "페이지 번호 (0부터 시작, 기본값 0)")
            Integer page,
            @Parameter(description = "페이지 크기 (1~20, 기본값 2)")
            Integer size,
            @Parameter(description = "사용자 현재 위도 (-90~90)")
            Double latitude,
            @Parameter(description = "사용자 현재 경도 (-180~180)")
            Double longitude
    );
}
