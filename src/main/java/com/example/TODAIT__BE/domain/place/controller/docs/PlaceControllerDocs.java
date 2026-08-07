package com.example.TODAIT__BE.domain.place.controller.docs;

import com.example.TODAIT__BE.domain.place.dto.response.PlaceDetailResponse;
import com.example.TODAIT__BE.domain.place.dto.response.PlaceSearchResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "PLACE", description = "장소 검색 및 상세 조회 API")
public interface PlaceControllerDocs {

    @Operation(
            summary = "[장소] 카카오 장소 검색 결과 조회",
            description = """
                    기준 장소 설정 화면에서 사용자가 입력한 검색어로
                    카카오 Local API의 장소 검색 결과를 조회합니다.

                    지원 지역과 지원 카테고리에 해당하는 장소만 반환하며,
                    이 API를 호출해도 내부 장소나 기준 장소가 저장되지 않습니다.
                    """
    )
    ResponseEntity<
            ApiResponse<PlaceSearchResponse.SearchResult>
            > searchPlaces(
            @Parameter(
                    description = "카카오 장소 검색에 사용할 검색어",
                    example = "연남동 카페"
            )
            String query
    );
    @Operation(
            summary = "[장소] 장소 카드 상세 조회",
            description = """
                    placeId에 해당하는 장소의 기본 정보·이미지·카테고리·분위기 태그·음식 카테고리·메뉴를 조회합니다.
                    노출 대상이 아닌 장소는 PLACE400, 존재하지 않는 장소는 PLACE404를 반환합니다.
                    """
    )
    ResponseEntity<ApiResponse<PlaceDetailResponse>>
    getPlaceDetail(
            @Parameter(
                    description = "상세 조회할 장소 ID"
            )
            Long placeId
    );
}
