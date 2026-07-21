package com.example.TODAIT__BE.domain.course.controller;

import com.example.TODAIT__BE.domain.course.code.CourseSuccessCode;
import com.example.TODAIT__BE.domain.course.controller.docs.CourseDraftControllerDocs;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftCreateResponse;
import com.example.TODAIT__BE.domain.course.service.CourseDraftService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course-drafts")
public class CourseDraftController implements CourseDraftControllerDocs {

    private final CourseDraftService courseDraftService;

    @PostMapping
    @Override
    public ApiResponse<CourseDraftCreateResponse> createCourseDraft(
            @AuthenticationPrincipal AuthMember authMember
    ) {
        CourseDraftCreateResponse result = courseDraftService.createCourseDraft(authMember.memberId());
        return ApiResponse.onSuccess(CourseSuccessCode.COURSE_DRAFT_CREATE_OK, result);
    }
}
