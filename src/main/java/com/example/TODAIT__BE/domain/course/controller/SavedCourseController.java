package com.example.TODAIT__BE.domain.course.controller;

import com.example.TODAIT__BE.domain.course.code.CourseSuccessCode;
import com.example.TODAIT__BE.domain.course.controller.docs.SavedCourseControllerDocs;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseOverviewResponse;
import com.example.TODAIT__BE.domain.course.service.SavedCourseService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseDetailResponse;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
public class SavedCourseController implements SavedCourseControllerDocs {

    private final SavedCourseService savedCourseService;

    @GetMapping("/me/overview")
    @Override
    public ResponseEntity<ApiResponse<SavedCourseOverviewResponse>>
    getSavedCourseOverview(
            @AuthenticationPrincipal AuthMember authMember
    ) {
        SavedCourseOverviewResponse result =
                savedCourseService.getSavedCourseOverview(
                        authMember.memberId()
                );

        return ResponseEntity
                .status(
                        CourseSuccessCode
                                .SAVED_COURSE_OVERVIEW_OK
                                .getStatus()
                )
                .body(
                        ApiResponse.onSuccess(
                                CourseSuccessCode
                                        .SAVED_COURSE_OVERVIEW_OK,
                                result
                        )
                );
    }

    @Override
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<SavedCourseDetailResponse>>
    getSavedCourseDetail(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long courseId
    ) {
        SavedCourseDetailResponse result =
                savedCourseService.getSavedCourseDetail(
                        authMember.memberId(),
                        courseId
                );

        return ResponseEntity.ok(
                ApiResponse.onSuccess(
                        CourseSuccessCode.SAVED_COURSE_DETAIL_OK,
                        result
                )
        );
    }
}
