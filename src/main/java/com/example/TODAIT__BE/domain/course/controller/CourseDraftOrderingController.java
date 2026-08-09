package com.example.TODAIT__BE.domain.course.controller;

import com.example.TODAIT__BE.domain.course.code.CourseDraftSuccessCode;
import com.example.TODAIT__BE.domain.course.controller.docs.CourseDraftOrderingControllerDocs;
import com.example.TODAIT__BE.domain.course.dto.response.OrderingEntryResponse;
import com.example.TODAIT__BE.domain.course.service.CourseDraftOrderingService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
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
public class CourseDraftOrderingController implements CourseDraftOrderingControllerDocs {

    private final CourseDraftOrderingService courseDraftOrderingService;

    @PatchMapping("/{courseDraftId}/ordering")
    @Override
    public ResponseEntity<ApiResponse<OrderingEntryResponse>> enterOrdering(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        OrderingEntryResponse result =
                courseDraftOrderingService.enterOrdering(
                        courseDraftId, authMember.memberId());

        return ResponseEntity
                .status(CourseDraftSuccessCode.ORDERING_ENTRY_OK.getStatus())
                .body(ApiResponse.onSuccess(
                        CourseDraftSuccessCode.ORDERING_ENTRY_OK, result));
    }
}
