package com.example.TODAIT__BE.domain.course.controller;

import com.example.TODAIT__BE.domain.course.code.CourseSuccessCode;
import com.example.TODAIT__BE.domain.course.controller.docs.CourseDraftFoodCategoryControllerDocs;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftFoodCategorySaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftFoodCategorySaveResponse;
import com.example.TODAIT__BE.domain.course.service.CourseDraftFoodCategoryService;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course-drafts")
public class CourseDraftFoodCategoryController implements CourseDraftFoodCategoryControllerDocs {

    private final CourseDraftFoodCategoryService courseDraftFoodCategoryService;

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
                .status(CourseSuccessCode.FOOD_CATEGORY_SAVE_OK.getStatus())
                .body(ApiResponse.onSuccess(CourseSuccessCode.FOOD_CATEGORY_SAVE_OK, result));
    }
}
