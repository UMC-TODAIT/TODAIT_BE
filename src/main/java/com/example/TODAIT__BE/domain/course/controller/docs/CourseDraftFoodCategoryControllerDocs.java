package com.example.TODAIT__BE.domain.course.controller.docs;

import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftFoodCategorySaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftFoodCategorySaveResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Course Draft", description = "임시 코스(코스 생성 플로우) 관련 API")
public interface CourseDraftFoodCategoryControllerDocs {

    @Operation(
            summary = "음식 카테고리 선택 저장",
            description = "임시 코스에 음식 카테고리 선택값을 diff 방식으로 저장하고, 상태를 BASE_PLACE_SELECTING으로 변경합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<CourseDraftFoodCategorySaveResponse>> saveFoodCategories(
            Long courseDraftId,
            AuthMember authMember,
            CourseDraftFoodCategorySaveRequest request
    );
}
