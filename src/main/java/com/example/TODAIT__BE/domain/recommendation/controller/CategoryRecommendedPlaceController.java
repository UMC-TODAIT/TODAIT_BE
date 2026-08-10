package com.example.TODAIT__BE.domain.recommendation.controller;

import com.example.TODAIT__BE.domain.recommendation.code.CategoryRecommendedPlaceSuccessCode;
import com.example.TODAIT__BE.domain.recommendation.controller.docs.CategoryRecommendedPlaceControllerDocs;
import com.example.TODAIT__BE.domain.recommendation.dto.response.CategoryRecommendedPlaceResponse;
import com.example.TODAIT__BE.domain.recommendation.service.CategoryRecommendedPlaceService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course-drafts")
public class CategoryRecommendedPlaceController
        implements CategoryRecommendedPlaceControllerDocs {

    private final CategoryRecommendedPlaceService categoryRecommendedPlaceService;

    @Override
    @GetMapping("/{courseDraftId}/recommended-places")
    public ResponseEntity<ApiResponse<CategoryRecommendedPlaceResponse>>
    getCategoryRecommendedPlaces(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long courseDraftId,
            @RequestParam String placeCategoryCode,
            @RequestParam(required = false) Integer size
    ) {
        CategoryRecommendedPlaceResponse result =
                categoryRecommendedPlaceService.getRecommendedPlaces(
                        authMember.memberId(),
                        courseDraftId,
                        placeCategoryCode,
                        size
                );

        return ResponseEntity
                .status(
                        CategoryRecommendedPlaceSuccessCode
                                .CATEGORY_RECOMMENDED_PLACE_LIST_OK
                                .getStatus()
                )
                .body(
                        ApiResponse.onSuccess(
                                CategoryRecommendedPlaceSuccessCode
                                        .CATEGORY_RECOMMENDED_PLACE_LIST_OK,
                                result
                        )
                );
    }
}
