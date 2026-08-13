package com.example.TODAIT__BE.domain.taxonomy.controller;

import com.example.TODAIT__BE.domain.taxonomy.code.PlaceCategorySuccessCode;
import com.example.TODAIT__BE.domain.taxonomy.controller.docs.PlaceCategoryControllerDocs;
import com.example.TODAIT__BE.domain.taxonomy.dto.response.PlaceCategoryListResponse;
import com.example.TODAIT__BE.domain.taxonomy.service.PlaceCategoryService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/place-categories")
public class PlaceCategoryController implements PlaceCategoryControllerDocs {

    private final PlaceCategoryService placeCategoryService;

    @GetMapping
    @Override
    public ResponseEntity<ApiResponse<PlaceCategoryListResponse>> getPlaceCategories() {
        PlaceCategoryListResponse result = placeCategoryService.getPlaceCategories();
        return ResponseEntity
                .status(PlaceCategorySuccessCode.PLACE_CATEGORY_LIST_OK.getStatus())
                .body(ApiResponse.onSuccess(PlaceCategorySuccessCode.PLACE_CATEGORY_LIST_OK, result));
    }
}
