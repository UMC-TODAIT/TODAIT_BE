package com.example.TODAIT__BE.domain.course.controller.docs;

import com.example.TODAIT__BE.domain.course.dto.request.CourseSaveRequest.SaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseSaveResponse.SaveResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "COURSE", description = "임시 코스 및 저장 코스 API")
public interface CourseSaveControllerDocs {

    @Operation(
            summary = "[코스 저장] 임시 코스 최종 저장",
            description = """
                    임시 코스를 최종 코스로 확정하여 저장합니다.

                    - 저장 가능 상태: SAVING
                    - 저장 결과: 사용자 개인 저장 코스
                    - 요청 body: title, memo
                    - 무드 태그, 음식 카테고리, 장소는 임시 코스에 저장된 선택값을 사용합니다.
                    """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<SaveResponse>> saveCourse(
            @PathVariable Long courseDraftId,
            @Parameter(hidden = true)
            AuthMember authMember,
            @RequestBody
            SaveRequest request
    );
}
