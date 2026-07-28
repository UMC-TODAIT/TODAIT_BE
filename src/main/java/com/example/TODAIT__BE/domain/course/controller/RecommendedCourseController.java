package com.example.TODAIT__BE.domain.course.controller;

import com.example.TODAIT__BE.domain.course.code.CourseSuccessCode;
import com.example.TODAIT__BE.domain.course.dto.response.RecommendedCourseDetailResponse;
import com.example.TODAIT__BE.domain.course.service.RecommendedCourseService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.TODAIT__BE.domain.course.controller.docs.RecommendedCourseControllerDocs;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommended-courses")
public class RecommendedCourseController
        implements RecommendedCourseControllerDocs {

    private final RecommendedCourseService recommendedCourseService;

    @GetMapping("/{courseId}")
    @Override
    public ResponseEntity<ApiResponse<RecommendedCourseDetailResponse>>
    getRecommendedCourseDetail(
            @PathVariable Long courseId
    ) {
        RecommendedCourseDetailResponse result =
                recommendedCourseService.getRecommendedCourseDetail(courseId);

        return ResponseEntity
                .status(
                        CourseSuccessCode
                                .RECOMMENDED_COURSE_DETAIL_OK
                                .getStatus()
                )
                .body(
                        ApiResponse.onSuccess(
                                CourseSuccessCode
                                        .RECOMMENDED_COURSE_DETAIL_OK,
                                result
                        )
                );
    }
}
