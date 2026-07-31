package com.example.TODAIT__BE.domain.place.controller.docs;

import com.example.TODAIT__BE.domain.place.dto.response.PlaceDetailResponse;
import com.example.TODAIT__BE.domain.place.dto.response.PlaceSearchResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Place", description = "장소 검색/상세 조회 API")
public interface PlaceControllerDocs {

    @Operation(summary = "장소명 검색", description = "keyword를 기준으로 노출 가능한 장소를 검색합니다.")
    ResponseEntity<ApiResponse<PlaceSearchResponse>> searchPlaces(String keyword);

    @Operation(
            summary = "장소 카드 상세 조회",
            description = """
                    placeId에 해당하는 장소의 기본 정보·이미지·카테고리·분위기 태그·음식 카테고리·메뉴를 조회합니다.
                    노출 대상이 아닌 장소는 PLACE400, 존재하지 않는 장소는 PLACE404를 반환합니다.
                    """
    )
    ResponseEntity<ApiResponse<PlaceDetailResponse>> getPlaceDetail(
            @Parameter(description = "상세 조회할 장소 ID") Long placeId
    );
}
