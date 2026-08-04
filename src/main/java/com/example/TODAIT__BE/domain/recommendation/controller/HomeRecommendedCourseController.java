package com.example.TODAIT__BE.domain.recommendation.controller;

import com.example.TODAIT__BE.domain.recommendation.code.RecommendationSuccessCode;
import com.example.TODAIT__BE.domain.recommendation.controller.docs.HomeRecommendedCourseControllerDocs;
import com.example.TODAIT__BE.domain.recommendation.dto.response.HomeRecommendedCourseListResponse;
import com.example.TODAIT__BE.domain.recommendation.service.HomeRecommendedCourseService;
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
@RequestMapping("/api/recommended-courses")
public class HomeRecommendedCourseController
        implements HomeRecommendedCourseControllerDocs {

    private final HomeRecommendedCourseService homeRecommendedCourseService;

    @GetMapping
    @Override
    public ResponseEntity<ApiResponse<HomeRecommendedCourseListResponse>>
    getHomeRecommendedCourses(
            @AuthenticationPrincipal AuthMember authMember,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size
    ) {
        HomeRecommendedCourseListResponse result =
                homeRecommendedCourseService.getHomeRecommendedCourses(
                        authMember.memberId(),
                        cursor,
                        size
                );

        return ResponseEntity
                .status(
                        RecommendationSuccessCode
                                .HOME_RECOMMENDED_COURSE_LIST_OK
                                .getStatus()
                )
                .body(
                        ApiResponse.onSuccess(
                                RecommendationSuccessCode
                                        .HOME_RECOMMENDED_COURSE_LIST_OK,
                                result
                        )
                );
    }
}
