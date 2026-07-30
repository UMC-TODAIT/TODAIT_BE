package com.example.TODAIT__BE.domain.place.controller.docs;

import com.example.TODAIT__BE.domain.place.dto.response.KakaoPlaceSearchResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Place", description = "장소 검색/상세 조회 API")
public interface PlaceControllerDocs {

    @Operation(
            summary = "카카오 장소 검색 결과 조회",
            description = """
                    기준 장소 설정 화면에서 사용자가 입력한 검색어로
                    카카오 Local API의 장소 검색 결과를 조회합니다.

                    지원 지역과 지원 카테고리에 해당하는 장소만 반환하며,
                    이 API를 호출해도 내부 장소나 기준 장소가 저장되지 않습니다.
                    """
    )
    ResponseEntity<
            ApiResponse<KakaoPlaceSearchResponse.SearchResult>
            > searchPlaces(
            @Parameter(
                    description = "카카오 장소 검색에 사용할 검색어",
                    example = "연남동 카페"
            )
            String query
    );
}
