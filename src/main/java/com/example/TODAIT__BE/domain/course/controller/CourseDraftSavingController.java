package com.example.TODAIT__BE.domain.course.controller;

import com.example.TODAIT__BE.domain.course.code.CourseSuccessCode;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftSavingEnterResponse;
import com.example.TODAIT__BE.domain.course.service.CourseDraftSavingService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "COURSE", description = "코스 도메인 API")
public class CourseDraftSavingController {

    private final CourseDraftSavingService courseDraftSavingService;

    @PatchMapping("/{courseDraftId}/saving")
    @Operation(summary = "[임시 코스] 저장 화면 진입", description = "임시 코스를 ORDERING에서 SAVING 상태로 전환합니다.")
    public ResponseEntity<ApiResponse<CourseDraftSavingEnterResponse>> enterSaving(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        CourseDraftSavingEnterResponse result =
                courseDraftSavingService.enterSaving(courseDraftId, authMember.memberId());

        return ResponseEntity
                .status(CourseSuccessCode.COURSE_DRAFT_SAVING_ENTER_OK.getStatus())
                .body(ApiResponse.onSuccess(CourseSuccessCode.COURSE_DRAFT_SAVING_ENTER_OK, result));
    }
}
