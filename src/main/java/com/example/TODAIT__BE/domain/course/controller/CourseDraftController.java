package com.example.TODAIT__BE.domain.course.controller;

import com.example.TODAIT__BE.domain.course.code.CourseSuccessCode;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftCreateResponse;
import com.example.TODAIT__BE.domain.course.service.CourseDraftService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course-drafts")
@Tag(name = "Course Draft", description = "임시 코스(코스 생성 플로우) 관련 API")
public class CourseDraftController {

    private final CourseDraftService courseDraftService;

    @PostMapping
    @Operation(summary = "임시 코스 생성", description = "코스 생성 플로우를 시작하기 위한 임시 코스를 생성합니다.")
    public ApiResponse<CourseDraftCreateResponse> createCourseDraft(
            @AuthenticationPrincipal AuthMember authMember
    ) {
        CourseDraftCreateResponse result = courseDraftService.createCourseDraft(authMember.memberId());
        return ApiResponse.onSuccess(CourseSuccessCode.COURSE_DRAFT_CREATE_OK, result);
    }
}
