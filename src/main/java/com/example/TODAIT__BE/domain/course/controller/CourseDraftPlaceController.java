package com.example.TODAIT__BE.domain.course.controller;

import com.example.TODAIT__BE.domain.course.code.CourseSuccessCode;
import com.example.TODAIT__BE.domain.course.dto.request.PlaceOrderUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.response.PlaceOrderUpdateResponse;
import com.example.TODAIT__BE.domain.course.service.CourseDraftPlaceService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course-drafts")
@Tag(name = "Course Draft Place", description = "임시 코스의 선택 장소 관리 API")
public class CourseDraftPlaceController {

    private final CourseDraftPlaceService courseDraftPlaceService;

    @PatchMapping("/{courseDraftId}/places/order")
    @Operation(summary = "선택 장소 순서 변경", description = "임시 코스에 담긴 선택 장소들의 방문 순서를 일괄 변경합니다.")
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
