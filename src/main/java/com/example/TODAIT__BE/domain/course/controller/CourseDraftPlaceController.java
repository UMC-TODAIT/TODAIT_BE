package com.example.TODAIT__BE.domain.course.controller;

import com.example.TODAIT__BE.domain.course.code.CourseSuccessCode;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftPlaceAddRequest;
import com.example.TODAIT__BE.domain.course.dto.request.PlaceOrderUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftPlaceAddResponse;
import com.example.TODAIT__BE.domain.course.dto.response.PlaceOrderUpdateResponse;
import com.example.TODAIT__BE.domain.course.service.CourseDraftPlaceService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course-drafts")
@Tag(name = "COURSE", description = "임시 코스 및 저장 코스 API")
public class CourseDraftPlaceController {

    private final CourseDraftPlaceService courseDraftPlaceService;

    @PostMapping("/{courseDraftId}/places")
    @Operation(
            summary = "[장소 선택] 선택 장소 추가",
            description = """
                    카테고리별 추천 장소 카드 중 하나를 현재 임시 코스에 선택 장소로 추가합니다.

                    - PLACE_SELECTING 상태에서만 호출 가능하며, 성공 후에도 PLACE_SELECTING을 유지합니다.
                    - 기준 장소와 동일한 장소, 이미 선택한 장소는 추가할 수 없습니다.
                    - 이미 선택된 카테고리(기준 장소 포함)와 같은 카테고리의 장소는 추가할 수 없습니다.
                    """
    )
    public ResponseEntity<ApiResponse<CourseDraftPlaceAddResponse>> addPlace(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember,
            @Valid @RequestBody CourseDraftPlaceAddRequest request
    ) {
        CourseDraftPlaceAddResponse result =
                courseDraftPlaceService.addPlace(courseDraftId, authMember.memberId(), request);
        return ResponseEntity
                .status(CourseSuccessCode.PLACE_ADD_OK.getStatus())
                .body(ApiResponse.onSuccess(CourseSuccessCode.PLACE_ADD_OK, result));
    }

    @PatchMapping("/{courseDraftId}/places/order")
    @Operation(
            summary = "[순서 설정] 선택 장소 순서 변경",
            description = """
                    임시 코스에 담긴 선택 장소들의 방문 순서를 일괄 변경합니다.

                    - BASE 장소는 요청에서 제외합니다.
                    - 선택 장소는 2번부터 연속된 방문 순서를 가져야 합니다.
                    - 성공 후 draftStatus는 ORDERING입니다.
                    """
    )
    public ResponseEntity<ApiResponse<PlaceOrderUpdateResponse>> updatePlaceOrder(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember,
            @Valid @RequestBody PlaceOrderUpdateRequest request
    ) {
        PlaceOrderUpdateResponse result =
                courseDraftPlaceService.updatePlaceOrder(courseDraftId, authMember.memberId(), request);
        return ResponseEntity
                .status(CourseSuccessCode.PLACE_ORDER_UPDATE_OK.getStatus())
                .body(ApiResponse.onSuccess(CourseSuccessCode.PLACE_ORDER_UPDATE_OK, result));
    }
}
