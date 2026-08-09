package com.example.TODAIT__BE.domain.course.controller;

import com.example.TODAIT__BE.domain.course.code.CourseSaveSuccessCode;
import com.example.TODAIT__BE.domain.course.controller.docs.CourseSaveControllerDocs;
import com.example.TODAIT__BE.domain.course.dto.request.CourseSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseSaveResponse;
import com.example.TODAIT__BE.domain.course.service.CourseSaveService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course-drafts")
public class CourseSaveController implements CourseSaveControllerDocs {

    private final CourseSaveService courseSaveService;

    @PostMapping("/{courseDraftId}/courses")
    @Override
    public ResponseEntity<ApiResponse<CourseSaveResponse>> saveCourse(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember,
            @RequestBody CourseSaveRequest request
    ) {
        CourseSaveResponse result = courseSaveService.saveCourse(courseDraftId, authMember.memberId(), request);
        return ResponseEntity
                .status(CourseSaveSuccessCode.COURSE_SAVE_OK.getStatus())
                .body(ApiResponse.onSuccess(CourseSaveSuccessCode.COURSE_SAVE_OK, result));
    }
}
