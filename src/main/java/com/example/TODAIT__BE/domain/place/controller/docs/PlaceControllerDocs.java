package com.example.TODAIT__BE.domain.place.controller.docs;

import com.example.TODAIT__BE.domain.place.dto.response.PlaceSearchResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Place", description = "장소 검색/상세 조회 API")
public interface PlaceControllerDocs {

    @Operation(summary = "장소명 검색", description = "keyword를 기준으로 노출 가능한 장소를 검색합니다.")
    ResponseEntity<ApiResponse<PlaceSearchResponse>> searchPlaces(String keyword);
}
