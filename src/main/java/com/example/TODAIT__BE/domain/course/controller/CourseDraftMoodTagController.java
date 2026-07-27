package com.example.TODAIT__BE.domain.course.controller;

import com.example.TODAIT__BE.domain.course.code.CourseSuccessCode;
import com.example.TODAIT__BE.domain.course.controller.docs.CourseDraftMoodTagControllerDocs;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftMoodTagSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftMoodTagSaveResponse;
import com.example.TODAIT__BE.domain.course.service.CourseDraftMoodTagService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course-drafts")
public class CourseDraftMoodTagController implements CourseDraftMoodTagControllerDocs {

    private final CourseDraftMoodTagService courseDraftMoodTagService;

    @PutMapping("/{courseDraftId}/mood-tags")
    @Override
    public ResponseEntity<ApiResponse<CourseDraftMoodTagSaveResponse>> saveMoodTags(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember,
            @RequestBody CourseDraftMoodTagSaveRequest request
    ) {
        CourseDraftMoodTagSaveResponse result = courseDraftMoodTagService.saveMoodTags(
                courseDraftId,
                authMember.memberId(),
                request
        );
        return ResponseEntity
                .status(CourseSuccessCode.MOOD_TAG_SAVE_OK.getStatus())
                .body(ApiResponse.onSuccess(CourseSuccessCode.MOOD_TAG_SAVE_OK, result));
    }
}
