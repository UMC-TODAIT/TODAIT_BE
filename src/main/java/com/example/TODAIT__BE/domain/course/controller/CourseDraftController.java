package com.example.TODAIT__BE.domain.course.controller;

import com.example.TODAIT__BE.domain.course.code.CourseDraftSuccessCode;
import com.example.TODAIT__BE.domain.course.controller.docs.CourseDraftBasePlaceControllerDocs;
import com.example.TODAIT__BE.domain.course.controller.docs.CourseDraftControllerDocs;
import com.example.TODAIT__BE.domain.course.controller.docs.CourseDraftFoodCategoryControllerDocs;
import com.example.TODAIT__BE.domain.course.controller.docs.CourseDraftMoodTagControllerDocs;
import com.example.TODAIT__BE.domain.course.controller.docs.CourseDraftOrderingControllerDocs;
import com.example.TODAIT__BE.domain.course.controller.docs.CourseDraftPlaceControllerDocs;
import com.example.TODAIT__BE.domain.course.controller.docs.CourseDraftSavingControllerDocs;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftBasePlaceSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftFoodCategorySaveRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftMoodTagSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftPlaceAddRequest;
import com.example.TODAIT__BE.domain.course.dto.request.PlaceOrderUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftBasePlaceSaveResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftCreateResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftFoodCategorySaveResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftMoodTagSaveResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftPlaceAddResponse;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftSavingEnterResponse;
import com.example.TODAIT__BE.domain.course.dto.response.OrderingEntryResponse;
import com.example.TODAIT__BE.domain.course.dto.response.PlaceOrderUpdateResponse;
import com.example.TODAIT__BE.domain.course.service.CourseDraftBasePlaceService;
import com.example.TODAIT__BE.domain.course.service.CourseDraftFoodCategoryService;
import com.example.TODAIT__BE.domain.course.service.CourseDraftMoodTagService;
import com.example.TODAIT__BE.domain.course.service.CourseDraftOrderingService;
import com.example.TODAIT__BE.domain.course.service.CourseDraftPlaceService;
import com.example.TODAIT__BE.domain.course.service.CourseDraftSavingService;
import com.example.TODAIT__BE.domain.course.service.CourseDraftService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
public class CourseDraftController implements
        CourseDraftControllerDocs,
        CourseDraftMoodTagControllerDocs,
        CourseDraftFoodCategoryControllerDocs,
        CourseDraftBasePlaceControllerDocs,
        CourseDraftPlaceControllerDocs,
        CourseDraftOrderingControllerDocs,
        CourseDraftSavingControllerDocs {

    private final CourseDraftService courseDraftService;
    private final CourseDraftMoodTagService courseDraftMoodTagService;
    private final CourseDraftFoodCategoryService courseDraftFoodCategoryService;
    private final CourseDraftBasePlaceService courseDraftBasePlaceService;
    private final CourseDraftPlaceService courseDraftPlaceService;
    private final CourseDraftOrderingService courseDraftOrderingService;
    private final CourseDraftSavingService courseDraftSavingService;

    @PostMapping
    @Override
    public ResponseEntity<ApiResponse<CourseDraftCreateResponse>> createCourseDraft(
            @AuthenticationPrincipal AuthMember authMember
    ) {
        CourseDraftCreateResponse result = courseDraftService.createCourseDraft(authMember.memberId());
        return ResponseEntity
                .status(CourseDraftSuccessCode.COURSE_DRAFT_CREATE_OK.getStatus())
                .body(ApiResponse.onSuccess(CourseDraftSuccessCode.COURSE_DRAFT_CREATE_OK, result));
    }

    @PutMapping("/{courseDraftId}/mood-tags")
    @Override
    public ResponseEntity<ApiResponse<CourseDraftMoodTagSaveResponse>> saveMoodTags(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember,
            @RequestBody CourseDraftMoodTagSaveRequest request
    ) {
        CourseDraftMoodTagSaveResponse result = courseDraftMoodTagService.saveMoodTags(
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
    public ResponseEntity<ApiResponse<CourseDraftFoodCategorySaveResponse>> saveFoodCategories(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember,
            @RequestBody CourseDraftFoodCategorySaveRequest request
    ) {
        CourseDraftFoodCategorySaveResponse result = courseDraftFoodCategoryService.saveFoodCategories(
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
    public ResponseEntity<ApiResponse<CourseDraftBasePlaceSaveResponse>> saveBasePlace(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember,
            @RequestBody CourseDraftBasePlaceSaveRequest request
    ) {
        CourseDraftBasePlaceSaveResponse result = courseDraftBasePlaceService.saveBasePlace(
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
    public ResponseEntity<ApiResponse<CourseDraftPlaceAddResponse>> addPlace(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember,
            @Valid @RequestBody CourseDraftPlaceAddRequest request
    ) {
        CourseDraftPlaceAddResponse result =
                courseDraftPlaceService.addPlace(courseDraftId, authMember.memberId(), request);
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
                courseDraftOrderingService.enterOrdering(courseDraftId, authMember.memberId());

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
                courseDraftPlaceService.updatePlaceOrder(courseDraftId, authMember.memberId(), request);
        return ResponseEntity
                .status(CourseDraftSuccessCode.PLACE_ORDER_UPDATE_OK.getStatus())
                .body(ApiResponse.onSuccess(CourseDraftSuccessCode.PLACE_ORDER_UPDATE_OK, result));
    }

    @PatchMapping("/{courseDraftId}/saving")
    @Override
    public ResponseEntity<ApiResponse<CourseDraftSavingEnterResponse>> enterSaving(
            @PathVariable Long courseDraftId,
            @AuthenticationPrincipal AuthMember authMember
    ) {
        CourseDraftSavingEnterResponse result =
                courseDraftSavingService.enterSaving(courseDraftId, authMember.memberId());

        return ResponseEntity
                .status(CourseDraftSuccessCode.COURSE_DRAFT_SAVING_ENTER_OK.getStatus())
                .body(ApiResponse.onSuccess(CourseDraftSuccessCode.COURSE_DRAFT_SAVING_ENTER_OK, result));
    }
}
