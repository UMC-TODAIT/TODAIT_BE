package com.example.TODAIT__BE.domain.course.controller;

import com.example.TODAIT__BE.domain.course.code.CourseSuccessCode;
import com.example.TODAIT__BE.domain.course.dto.response.OrderingEntryResponse;
import com.example.TODAIT__BE.domain.course.service.CourseDraftOrderingService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course-drafts")
@Tag(name = "Course Draft Ordering", description = "임시 코스 순서 설정 화면 진입 API")
public class CourseDraftOrderingController {

    private final CourseDraftOrderingService courseDraftOrderingService;

    @PatchMapping("/{courseDraftId}/ordering")
    @Operation(
            summary = "임시 코스 순서 설정 화면 진입",
            description = """
                    장소 선택을 완료하고 드래그 순서 설정 화면에 진입할 때 호출합니다.
                    장소 구성 무결성을 검증한 뒤 상태가 PLACE_SELECTING 이면 ORDERING 으로 전환하고,
                    이미 ORDERING 이면 상태 변경 없이 동일한 성공 응답을 반환합니다(멱등).
                    실제 순서 변경은 별도의 순서 변경 API를 사용합니다.
                    """
    )
    public ResponseEntity<ApiResponse<OrderingEntryResponse>> enterOrdering(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        OrderingEntryResponse result =
                courseDraftOrderingService.enterOrdering(
                        courseDraftId, authMember.memberId());

        return ResponseEntity
                .status(CourseSuccessCode.ORDERING_ENTRY_OK.getStatus())
                .body(ApiResponse.onSuccess(
                        CourseSuccessCode.ORDERING_ENTRY_OK, result));
    }
}
