package com.example.TODAIT__BE.domain.recommendation.controller;

import com.example.TODAIT__BE.domain.recommendation.code.RecommendationSuccessCode;
import com.example.TODAIT__BE.domain.recommendation.controller.docs.HomeRecommendedPlaceControllerDocs;
import com.example.TODAIT__BE.domain.recommendation.dto.response.HomeRecommendedPlaceListResponse;
import com.example.TODAIT__BE.domain.recommendation.service.HomeRecommendedPlaceService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommended-places")
public class HomeRecommendedPlaceController
        implements HomeRecommendedPlaceControllerDocs {

    private final HomeRecommendedPlaceService homeRecommendedPlaceService;

    @GetMapping
    @Override
    public ResponseEntity<ApiResponse<HomeRecommendedPlaceListResponse>>
    getHomeRecommendedPlaces(
            @AuthenticationPrincipal AuthMember authMember,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude
    ) {
        HomeRecommendedPlaceListResponse result =
                homeRecommendedPlaceService.getHomeRecommendedPlaces(
                        authMember.memberId(),
                        cursor,
                        size,
                        latitude,
                        longitude
                );

        return ResponseEntity
                .status(
                        RecommendationSuccessCode
                                .HOME_RECOMMENDED_PLACE_LIST_OK
                                .getStatus()
                )
                .body(
                        ApiResponse.onSuccess(
                                RecommendationSuccessCode
                                        .HOME_RECOMMENDED_PLACE_LIST_OK,
                                result
                        )
                );
    }
}
