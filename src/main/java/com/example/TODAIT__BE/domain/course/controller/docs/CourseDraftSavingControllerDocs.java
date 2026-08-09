package com.example.TODAIT__BE.domain.course.controller.docs;

import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftSavingEnterResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "COURSE", description = "임시 코스 및 저장 코스 API")
public interface CourseDraftSavingControllerDocs {

    @Operation(
            summary = "[저장 준비] 저장 화면 진입",
            description = """
                    임시 코스를 ORDERING에서 SAVING 상태로 전환합니다.

                    저장 전 기준 장소와 선택 장소 구성이 유효한지 확인합니다.
                    """
    )
    ResponseEntity<ApiResponse<CourseDraftSavingEnterResponse>> enterSaving(
            Long courseDraftId,
            AuthMember authMember
    );
}
