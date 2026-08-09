package com.example.TODAIT__BE.domain.course.controller.docs;

import com.example.TODAIT__BE.domain.course.dto.request.CourseSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseSaveResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "COURSE", description = "임시 코스 및 저장 코스 API")
public interface CourseSaveControllerDocs {

    @Operation(
            summary = "[코스 저장] 임시 코스 최종 저장",
            description = """
                    임시 코스를 최종 코스로 확정하여 저장합니다.

                    - 저장 가능 상태: SAVING
                    - 저장 결과: 사용자 개인 저장 코스
                    """
    )
    ResponseEntity<ApiResponse<CourseSaveResponse>> saveCourse(
            Long courseDraftId,
            AuthMember authMember,
            CourseSaveRequest request
    );
}
