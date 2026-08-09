package com.example.TODAIT__BE.domain.course.controller;

import com.example.TODAIT__BE.domain.course.code.CourseSuccessCode;
import com.example.TODAIT__BE.domain.course.controller.docs.SavedCourseDeleteControllerDocs;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseDeleteResponse;
import com.example.TODAIT__BE.domain.course.service.SavedCourseDeleteService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
public class SavedCourseDeleteController
        implements SavedCourseDeleteControllerDocs {

    private final SavedCourseDeleteService savedCourseDeleteService;

    @Override
    @DeleteMapping("/{courseId}")
    public ResponseEntity<ApiResponse<SavedCourseDeleteResponse>>
    deleteSavedCourse(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long courseId
    ) {
        SavedCourseDeleteResponse result =
                savedCourseDeleteService.deleteSavedCourse(
                        courseId,
                        authMember.memberId()
                );

        return ResponseEntity
                .status(
                        CourseSuccessCode
                                .SAVED_COURSE_DELETE_OK
                                .getStatus()
                )
                .body(
                        ApiResponse.onSuccess(
                                CourseSuccessCode
                                        .SAVED_COURSE_DELETE_OK,
                                result
                        )
                );
    }
}
