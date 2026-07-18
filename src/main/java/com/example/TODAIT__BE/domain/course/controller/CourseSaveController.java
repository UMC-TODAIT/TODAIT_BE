package com.example.TODAIT__BE.domain.course.controller;

import com.example.TODAIT__BE.domain.course.code.CourseSuccessCode;
import com.example.TODAIT__BE.domain.course.dto.request.CourseSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseSaveResponse;
import com.example.TODAIT__BE.domain.course.service.CourseSaveService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course-drafts")
@Tag(name = "Course Save", description = "임시 코스를 최종 코스로 저장하는 API")
public class CourseSaveController {

    private final CourseSaveService courseSaveService;

    @PostMapping("/{courseDraftId}/courses")
    @Operation(summary = "코스 저장", description = "임시 코스를 최종 코스로 확정하여 저장합니다.")
    public ApiResponse<CourseSaveResponse> saveCourse(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember,
            @RequestBody CourseSaveRequest request
    ) {
        CourseSaveResponse result = courseSaveService.saveCourse(courseDraftId, authMember.memberId(), request);
        return ApiResponse.onSuccess(CourseSuccessCode.COURSE_SAVE_OK, result);
    }
}
