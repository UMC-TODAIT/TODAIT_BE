package com.example.TODAIT__BE.domain.taxonomy.controller.docs;

import com.example.TODAIT__BE.domain.taxonomy.dto.response.PlaceCategoryListResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "TAXONOMY", description = "장소 카테고리 등 기준 정보 API")
public interface PlaceCategoryControllerDocs {

    @Operation(
            summary = "[장소 카테고리] 장소 카테고리 목록 조회",
            description = """
                    활성화된 장소 대분류 목록을 조회합니다.

                    - 카페
                    - 식당
                    - 액티비티
                    - 바
                    - 기타
                    """
    )
    ResponseEntity<ApiResponse<PlaceCategoryListResponse>> getPlaceCategories();
}
