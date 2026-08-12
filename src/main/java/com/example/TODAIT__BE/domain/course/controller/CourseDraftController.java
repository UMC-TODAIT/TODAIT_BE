package com.example.TODAIT__BE.domain.course.controller;

import com.example.TODAIT__BE.domain.course.code.CourseDraftSuccessCode;
import com.example.TODAIT__BE.domain.course.controller.docs.CourseDraftControllerDocs;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.BasePlaceSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.FoodCategorySaveRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.MoodTagSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.PlaceAddRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.PlaceOrderUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.StatusUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.BasePlaceSaveResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.CreateResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.CurrentResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.FoodCategorySaveResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.MoodTagSaveResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.OrderingEntryResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.PlaceAddResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.PlaceOrderUpdateResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.SavingEnterResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.StatusUpdateResponse;
import com.example.TODAIT__BE.domain.course.service.CourseDraftService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course-drafts")
public class CourseDraftController implements CourseDraftControllerDocs {

    private final CourseDraftService courseDraftService;

    @PostMapping
    @Override
    public ResponseEntity<ApiResponse<CreateResponse>> createCourseDraft(
            @AuthenticationPrincipal AuthMember authMember
    ) {
        CreateResponse result = courseDraftService.createCourseDraft(authMember.memberId());
        return ResponseEntity
                .status(CourseDraftSuccessCode.COURSE_DRAFT_CREATE_OK.getStatus())
                .body(ApiResponse.onSuccess(CourseDraftSuccessCode.COURSE_DRAFT_CREATE_OK, result));
    }

    @GetMapping("/current")
    @Override
    public ResponseEntity<ApiResponse<CurrentResponse>> getCurrentCourseDraft(
            @AuthenticationPrincipal AuthMember authMember
    ) {
        CurrentResponse result = courseDraftService.getCurrentCourseDraft(authMember.memberId());
        return ResponseEntity
                .status(CourseDraftSuccessCode.COURSE_DRAFT_CURRENT_OK.getStatus())
                .body(ApiResponse.onSuccess(CourseDraftSuccessCode.COURSE_DRAFT_CURRENT_OK, result));
    }

    @PutMapping("/{courseDraftId}/mood-tags")
    @Override
    public ResponseEntity<ApiResponse<MoodTagSaveResponse>> saveMoodTags(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember,
            @RequestBody MoodTagSaveRequest request
    ) {
        MoodTagSaveResponse result = courseDraftService.saveMoodTags(
                courseDraftId,
                authMember.memberId(),
                request
        );
        return ResponseEntity
                .status(CourseDraftSuccessCode.MOOD_TAG_SAVE_OK.getStatus())
                .body(ApiResponse.onSuccess(CourseDraftSuccessCode.MOOD_TAG_SAVE_OK, result));
    }

    @PutMapping("/{courseDraftId}/food-categories")
    @Override
    public ResponseEntity<ApiResponse<FoodCategorySaveResponse>> saveFoodCategories(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember,
            @RequestBody FoodCategorySaveRequest request
    ) {
        FoodCategorySaveResponse result = courseDraftService.saveFoodCategories(
                courseDraftId,
                authMember.memberId(),
                request
        );
        return ResponseEntity
                .status(CourseDraftSuccessCode.FOOD_CATEGORY_SAVE_OK.getStatus())
                .body(ApiResponse.onSuccess(CourseDraftSuccessCode.FOOD_CATEGORY_SAVE_OK, result));
    }

    @PatchMapping("/{courseDraftId}/base-place")
    @Override
    public ResponseEntity<ApiResponse<BasePlaceSaveResponse>> saveBasePlace(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember,
            @RequestBody BasePlaceSaveRequest request
    ) {
        BasePlaceSaveResponse result = courseDraftService.saveBasePlace(
                courseDraftId,
                authMember.memberId(),
                request
        );
        return ResponseEntity
                .status(CourseDraftSuccessCode.BASE_PLACE_SAVE_OK.getStatus())
                .body(ApiResponse.onSuccess(CourseDraftSuccessCode.BASE_PLACE_SAVE_OK, result));
    }

    @PostMapping("/{courseDraftId}/places")
    @Override
    public ResponseEntity<ApiResponse<PlaceAddResponse>> addPlace(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember,
            @Valid @RequestBody PlaceAddRequest request
    ) {
        PlaceAddResponse result =
                courseDraftService.addPlace(courseDraftId, authMember.memberId(), request);
        return ResponseEntity
                .status(CourseDraftSuccessCode.PLACE_ADD_OK.getStatus())
                .body(ApiResponse.onSuccess(CourseDraftSuccessCode.PLACE_ADD_OK, result));
    }

    @PatchMapping("/{courseDraftId}/ordering")
    @Override
    public ResponseEntity<ApiResponse<OrderingEntryResponse>> enterOrdering(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        OrderingEntryResponse result =
                courseDraftService.enterOrdering(courseDraftId, authMember.memberId());

        return ResponseEntity
                .status(CourseDraftSuccessCode.ORDERING_ENTRY_OK.getStatus())
                .body(ApiResponse.onSuccess(CourseDraftSuccessCode.ORDERING_ENTRY_OK, result));
    }

    @PatchMapping("/{courseDraftId}/places/order")
    @Override
    public ResponseEntity<ApiResponse<PlaceOrderUpdateResponse>> updatePlaceOrder(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember,
            @Valid @RequestBody PlaceOrderUpdateRequest request
    ) {
        PlaceOrderUpdateResponse result =
                courseDraftService.updatePlaceOrder(courseDraftId, authMember.memberId(), request);
        return ResponseEntity
                .status(CourseDraftSuccessCode.PLACE_ORDER_UPDATE_OK.getStatus())
                .body(ApiResponse.onSuccess(CourseDraftSuccessCode.PLACE_ORDER_UPDATE_OK, result));
    }

    @PatchMapping("/{courseDraftId}/saving")
    @Override
    public ResponseEntity<ApiResponse<SavingEnterResponse>> enterSaving(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        SavingEnterResponse result =
                courseDraftService.enterSaving(courseDraftId, authMember.memberId());

        return ResponseEntity
                .status(CourseDraftSuccessCode.COURSE_DRAFT_SAVING_ENTER_OK.getStatus())
                .body(ApiResponse.onSuccess(CourseDraftSuccessCode.COURSE_DRAFT_SAVING_ENTER_OK, result));
    }

    @PatchMapping("/{courseDraftId}/status")
    @Override
    public ResponseEntity<ApiResponse<StatusUpdateResponse>> updateStatus(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        StatusUpdateResponse result =
                courseDraftService.updateStatus(courseDraftId, authMember.memberId(), request);

        return ResponseEntity
                .status(CourseDraftSuccessCode.COURSE_DRAFT_STATUS_UPDATE_OK.getStatus())
                .body(ApiResponse.onSuccess(CourseDraftSuccessCode.COURSE_DRAFT_STATUS_UPDATE_OK, result));
    }
}
