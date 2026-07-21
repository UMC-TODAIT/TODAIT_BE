package com.example.TODAIT__BE.domain.place.controller;

import com.example.TODAIT__BE.domain.place.code.PlaceSuccessCode;
import com.example.TODAIT__BE.domain.place.dto.response.PlaceSearchResponse;
import com.example.TODAIT__BE.domain.place.service.PlaceService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
@Tag(name = "Place", description = "장소 검색/상세 조회 API")
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping("/search")
    @Operation(summary = "장소명 검색", description = "keyword를 기준으로 노출 가능한 장소를 검색합니다.")
    public ResponseEntity<ApiResponse<PlaceSearchResponse>> searchPlaces(
            @RequestParam(required = false) String keyword
    ) {
        PlaceSearchResponse result = placeService.searchPlaces(keyword);
        return ApiResponse.onSuccessResponse(PlaceSuccessCode.PLACE_SEARCH_OK, result);
    }
}
