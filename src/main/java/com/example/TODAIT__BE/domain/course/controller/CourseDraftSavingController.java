package com.example.TODAIT__BE.domain.course.controller;

import com.example.TODAIT__BE.domain.course.code.CourseDraftSuccessCode;
import com.example.TODAIT__BE.domain.course.controller.docs.CourseDraftSavingControllerDocs;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftSavingEnterResponse;
import com.example.TODAIT__BE.domain.course.service.CourseDraftSavingService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course-drafts")
public class CourseDraftSavingController implements CourseDraftSavingControllerDocs {

    private final CourseDraftSavingService courseDraftSavingService;

    @PatchMapping("/{courseDraftId}/saving")
    @Override
    public ResponseEntity<ApiResponse<CourseDraftSavingEnterResponse>> enterSaving(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        CourseDraftSavingEnterResponse result =
                courseDraftSavingService.enterSaving(courseDraftId, authMember.memberId());

        return ResponseEntity
                .status(CourseDraftSuccessCode.COURSE_DRAFT_SAVING_ENTER_OK.getStatus())
                .body(ApiResponse.onSuccess(CourseDraftSuccessCode.COURSE_DRAFT_SAVING_ENTER_OK, result));
    }
}
