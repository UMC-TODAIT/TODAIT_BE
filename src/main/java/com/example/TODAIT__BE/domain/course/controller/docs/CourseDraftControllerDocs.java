package com.example.TODAIT__BE.domain.course.controller.docs;

import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftCreateResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "COURSE",
        description = "임시 코스 및 저장 코스 API"
)
public interface CourseDraftControllerDocs {

    @Operation(
            summary = "[임시 코스 생성] 코스 생성 시작",
            description = """
                    코스 생성 플로우를 시작하기 위한 새로운 임시 코스를 생성합니다.

                    기존 미완료 임시 코스가 존재하더라도 매 요청마다 새로운 Draft를 생성합니다.

                    - 최초 상태: MOOD_SELECTING
                    - 인증: Access Token 필요
                    """
    )
    ResponseEntity<ApiResponse<CourseDraftCreateResponse>>
    createCourseDraft(AuthMember authMember);
}
