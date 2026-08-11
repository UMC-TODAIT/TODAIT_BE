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
            summary = "[장소 검색] 카카오 장소 검색",
            description = """
                    기준 장소 설정 화면에서 사용자가 입력한 검색어로
                    카카오 Local API의 장소 검색 결과를 조회합니다.

                    - 반환 대상: 지원 지역에 해당하는 장소
                    - 카테고리: 기존 분류에 해당하지 않는 장소는 OTHER(기타)로 반환
                    - 페이지 조회: 첫 요청에서는 cursor를 생략하고, 다음 요청부터 응답의 nextCursor 사용
                    - 저장 여부: 이 API 호출만으로 내부 장소나 기준 장소는 저장되지 않음
                    """
    )
    ResponseEntity<
            ApiResponse<PlaceSearchResponse.SearchResult>
            > searchPlaces(
            @Parameter(
                    description = "카카오 장소 검색에 사용할 검색어",
                    example = "연남동 카페"
            )
            String query,
            @Parameter(
                    description = "다음 검색 위치(1~45). 첫 요청에서는 생략하고 응답의 nextCursor를 전달합니다.",
                    example = "2"
            )
            Integer cursor,
            @Parameter(
                    description = "한 번에 카카오에서 조회할 장소 개수(1~15, 기본값 10)",
                    example = "10"
            )
            Integer size
    );
    @Operation(
            summary = "[장소 상세] 장소 카드 상세 조회",
            description = """
                    placeId에 해당하는 장소의 상세 정보를 조회합니다.

                    - 기본 정보, 이미지, 카테고리
                    - 분위기 태그, 음식 카테고리, 메뉴
                    - 노출 대상이 아닌 장소: PLACE400_4
                    - 존재하지 않는 장소: PLACE404
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
