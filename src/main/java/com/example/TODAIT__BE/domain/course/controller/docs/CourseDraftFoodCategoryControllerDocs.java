package com.example.TODAIT__BE.domain.course.controller.docs;

import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftFoodCategorySaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftFoodCategorySaveResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "COURSE", description = "임시 코스 및 저장 코스 API")
public interface CourseDraftFoodCategoryControllerDocs {

    @Operation(
            summary = "[임시 코스] 음식 카테고리 선택 저장",
            description = "임시 코스에 음식 카테고리 선택값 전체를 PUT 방식으로 교체 저장합니다. "
                    + "FOOD_SELECTING 상태에서 최초 저장 시 BASE_PLACE_SELECTING으로 전이하며, "
                    + "BASE_PLACE_SELECTING 상태에서는 재호출 시 카테고리만 교체하고 상태는 유지합니다."
    )
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<CourseDraftFoodCategorySaveResponse>> saveFoodCategories(
            @PathVariable Long courseDraftId,
            AuthMember authMember,
            CourseDraftFoodCategorySaveRequest request
    );
}
