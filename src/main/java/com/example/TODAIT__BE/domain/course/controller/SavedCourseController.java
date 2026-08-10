package com.example.TODAIT__BE.domain.course.controller;

import com.example.TODAIT__BE.domain.course.code.SavedCourseSuccessCode;
import com.example.TODAIT__BE.domain.course.controller.docs.SavedCourseControllerDocs;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseResponse.OverviewResponse;
import com.example.TODAIT__BE.domain.course.service.SavedCourseService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseResponse.DetailResponse;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.TODAIT__BE.domain.course.dto.request.SavedCourseMemoUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.request.SavedCoursePlaceMemoUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCourseMemoUpdateResponse;
import com.example.TODAIT__BE.domain.course.dto.response.SavedCoursePlaceMemoUpdateResponse;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
public class SavedCourseController implements SavedCourseControllerDocs {

    private final SavedCourseService savedCourseService;

    @GetMapping("/me/overview")
    @Override
    public ResponseEntity<ApiResponse<OverviewResponse>>
    getSavedCourseOverview(
            @AuthenticationPrincipal AuthMember authMember
    ) {
        OverviewResponse result =
                savedCourseService.getSavedCourseOverview(
                        authMember.memberId()
                );

        return ResponseEntity
                .status(
                        SavedCourseSuccessCode
                                .SAVED_COURSE_OVERVIEW_OK
                                .getStatus()
                )
                .body(
                        ApiResponse.onSuccess(
                                SavedCourseSuccessCode
                                        .SAVED_COURSE_OVERVIEW_OK,
                                result
                        )
                );
    }

    @Override
    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<DetailResponse>>
    getSavedCourseDetail(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long courseId
    ) {
        DetailResponse result =
                savedCourseService.getSavedCourseDetail(
                        authMember.memberId(),
                        courseId
                );

        return ResponseEntity.ok(
                ApiResponse.onSuccess(
                        SavedCourseSuccessCode.SAVED_COURSE_DETAIL_OK,
                        result
                )
        );
    }

    @Override
    @PatchMapping("/{courseId}/memo")
    public ResponseEntity<ApiResponse<SavedCourseMemoUpdateResponse>>
    updateSavedCourseMemo(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long courseId,
            @RequestBody SavedCourseMemoUpdateRequest request
    ) {
        SavedCourseMemoUpdateResponse result =
                savedCourseService.updateSavedCourseMemo(
                        authMember.memberId(),
                        courseId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.onSuccess(
                        SavedCourseSuccessCode.SAVED_COURSE_MEMO_UPDATE_OK,
                        result
                )
        );
    }

    @Override
    @PatchMapping("/{courseId}/places/{coursePlaceId}/memo")
    public ResponseEntity<ApiResponse<SavedCoursePlaceMemoUpdateResponse>>
    updateSavedCoursePlaceMemo(
            @AuthenticationPrincipal AuthMember authMember,
            @PathVariable Long courseId,
            @PathVariable Long coursePlaceId,
            @RequestBody SavedCoursePlaceMemoUpdateRequest request
    ) {
        SavedCoursePlaceMemoUpdateResponse result =
                savedCourseService.updateSavedCoursePlaceMemo(
                        authMember.memberId(),
                        courseId,
                        coursePlaceId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.onSuccess(
                        SavedCourseSuccessCode.SAVED_COURSE_PLACE_MEMO_UPDATE_OK,
                        result
                )
        );
    }



}
