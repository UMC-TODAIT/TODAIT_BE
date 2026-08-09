package com.example.TODAIT__BE.domain.recommendation.controller;

import com.example.TODAIT__BE.domain.recommendation.code.HotPlaceRecommendationSuccessCode;
import com.example.TODAIT__BE.domain.recommendation.controller.docs.HotPlaceRecommendationControllerDocs;
import com.example.TODAIT__BE.domain.recommendation.dto.response.HotPlaceRecommendationResponse;
import com.example.TODAIT__BE.domain.recommendation.service.HotPlaceRecommendationService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course-drafts")
public class HotPlaceRecommendationController implements HotPlaceRecommendationControllerDocs {

    private final HotPlaceRecommendationService hotPlaceRecommendationService;

    @Override
    @GetMapping("/{courseDraftId}/hot-places")
    public ResponseEntity<ApiResponse<HotPlaceRecommendationResponse.Result>> getHotPlaces(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long courseDraftId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Integer size
    ) {
        HotPlaceRecommendationResponse.Result result =
                hotPlaceRecommendationService.getHotPlaces(
                        authMember.memberId(),
                        courseDraftId,
                        latitude,
                        longitude,
                        size
                );

        return ResponseEntity
                .status(
                        HotPlaceRecommendationSuccessCode
                                .HOT_PLACE_LIST_OK
                                .getStatus()
                )
                .body(
                        ApiResponse.onSuccess(
                                HotPlaceRecommendationSuccessCode
                                        .HOT_PLACE_LIST_OK,
                                result
                        )
                );
    }
}
