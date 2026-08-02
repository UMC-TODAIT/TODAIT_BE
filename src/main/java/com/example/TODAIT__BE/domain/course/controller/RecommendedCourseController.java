package com.example.TODAIT__BE.domain.course.controller;

import com.example.TODAIT__BE.domain.course.code.CourseSuccessCode;
import com.example.TODAIT__BE.domain.course.controller.docs.RecommendedCourseControllerDocs;
import com.example.TODAIT__BE.domain.course.dto.response.RecommendedCourseDetailResponse;
import com.example.TODAIT__BE.domain.course.dto.response.RecommendedCourseSaveResponse;
import com.example.TODAIT__BE.domain.course.service.RecommendedCourseSaveService;
import com.example.TODAIT__BE.domain.course.service.RecommendedCourseService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommended-courses")
public class RecommendedCourseController
        implements RecommendedCourseControllerDocs {

    private final RecommendedCourseService recommendedCourseService;
    private final RecommendedCourseSaveService
            recommendedCourseSaveService;

    @Override
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<RecommendedCourseDetailResponse>>
    getRecommendedCourseDetail(
            @PathVariable Long courseId
    ) {
        RecommendedCourseDetailResponse result =
                recommendedCourseService
                        .getRecommendedCourseDetail(courseId);

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

    @Override
    @PostMapping("/{courseId}/save")
    public ResponseEntity<ApiResponse<RecommendedCourseSaveResponse>>
    saveRecommendedCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        RecommendedCourseSaveResponse result =
                recommendedCourseSaveService
                        .saveRecommendedCourse(
                                courseId,
                                authMember.memberId()
                        );

        return ResponseEntity
                .status(
                        CourseSuccessCode
                                .RECOMMENDED_COURSE_SAVE_OK
                                .getStatus()
                )
                .body(
                        ApiResponse.onSuccess(
                                CourseSuccessCode
                                        .RECOMMENDED_COURSE_SAVE_OK,
                                result
                        )
                );
    }
}
