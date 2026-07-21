package com.example.TODAIT__BE.domain.taxonomy.controller;

import com.example.TODAIT__BE.domain.taxonomy.code.TaxonomySuccessCode;
import com.example.TODAIT__BE.domain.taxonomy.dto.response.PlaceCategoryListResponse;
import com.example.TODAIT__BE.domain.taxonomy.service.PlaceCategoryService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/place-categories")
@Tag(name = "Taxonomy", description = "장소 대분류/지역 등 기준 정보 조회 API")
public class PlaceCategoryController {

    private final PlaceCategoryService placeCategoryService;

    @GetMapping
    @Operation(summary = "장소 카테고리 목록 조회", description = "활성화된 장소 대분류(카페/식당/액티비티/바) 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<PlaceCategoryListResponse>> getPlaceCategories() {
        PlaceCategoryListResponse result = placeCategoryService.getPlaceCategories();
        return ApiResponse.onSuccessResponse(TaxonomySuccessCode.PLACE_CATEGORY_LIST_OK, result);
    }
}
