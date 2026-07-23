package com.example.TODAIT__BE.domain.course.controller.docs;

import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftCreateResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Course Draft", description = "임시 코스(코스 생성 플로우) 관련 API")
public interface CourseDraftControllerDocs {

    @Operation(summary = "임시 코스 생성", description = "코스 생성 플로우를 시작하기 위한 임시 코스를 생성합니다.")
    ResponseEntity<ApiResponse<CourseDraftCreateResponse>> createCourseDraft(AuthMember authMember);
}
