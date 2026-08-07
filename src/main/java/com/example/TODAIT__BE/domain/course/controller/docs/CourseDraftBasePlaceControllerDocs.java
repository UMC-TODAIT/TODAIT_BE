package com.example.TODAIT__BE.domain.course.controller.docs;

import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftBasePlaceSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftBasePlaceSaveResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Course Draft", description = "임시 코스(코스 생성 플로우) 관련 API")
public interface CourseDraftBasePlaceControllerDocs {

    @Operation(
            summary = "기준 장소 설정",
            description = "임시 코스의 기준 장소를 저장합니다. 내부 DB에 존재하는 장소는 placeId로, "
                    + "카카오 검색 결과 중 내부 DB에 없는 장소는 externalPlace로 전달합니다. "
                    + "BASE_PLACE_SELECTING 상태에서만 호출 가능하며, 성공 시 PLACE_SELECTING으로 전이합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<CourseDraftBasePlaceSaveResponse>> saveBasePlace(
            @PathVariable Long courseDraftId,
            AuthMember authMember,
            CourseDraftBasePlaceSaveRequest request
    );
}
