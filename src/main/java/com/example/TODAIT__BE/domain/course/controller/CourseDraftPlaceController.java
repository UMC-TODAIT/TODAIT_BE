package com.example.TODAIT__BE.domain.course.controller;

import com.example.TODAIT__BE.domain.course.code.CourseSuccessCode;
import com.example.TODAIT__BE.domain.course.controller.docs.CourseDraftPlaceControllerDocs;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftPlaceAddRequest;
import com.example.TODAIT__BE.domain.course.dto.request.PlaceOrderUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftPlaceAddResponse;
import com.example.TODAIT__BE.domain.course.dto.response.PlaceOrderUpdateResponse;
import com.example.TODAIT__BE.domain.course.service.CourseDraftPlaceService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
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
public class CourseDraftPlaceController implements CourseDraftPlaceControllerDocs {

    private final CourseDraftPlaceService courseDraftPlaceService;

    @PostMapping("/{courseDraftId}/places")
    @Override
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
    @Override
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
