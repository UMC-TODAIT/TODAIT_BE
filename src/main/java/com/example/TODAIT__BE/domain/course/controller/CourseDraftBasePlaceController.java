package com.example.TODAIT__BE.domain.course.controller;

import com.example.TODAIT__BE.domain.course.code.CourseSuccessCode;
import com.example.TODAIT__BE.domain.course.controller.docs.CourseDraftBasePlaceControllerDocs;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftBasePlaceSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftBasePlaceSaveResponse;
import com.example.TODAIT__BE.domain.course.service.CourseDraftBasePlaceService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course-drafts")
public class CourseDraftBasePlaceController implements CourseDraftBasePlaceControllerDocs {

    private final CourseDraftBasePlaceService courseDraftBasePlaceService;

    @PatchMapping("/{courseDraftId}/base-place")
    @Override
    public ResponseEntity<ApiResponse<CourseDraftBasePlaceSaveResponse>> saveBasePlace(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember,
            @RequestBody CourseDraftBasePlaceSaveRequest request
    ) {
        CourseDraftBasePlaceSaveResponse result = courseDraftBasePlaceService.saveBasePlace(
                courseDraftId,
                authMember.memberId(),
                request
        );
        return ResponseEntity
                .status(CourseSuccessCode.BASE_PLACE_SAVE_OK.getStatus())
                .body(ApiResponse.onSuccess(CourseSuccessCode.BASE_PLACE_SAVE_OK, result));
    }
}
