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
            summary = "[음식 선택] 음식 카테고리 저장",
            description = """
                    임시 코스에 음식 카테고리 선택값 전체를 PUT 방식으로 교체 저장합니다.

                    - 선택 개수: 1개 이상
                    - FOOD_SELECTING 상태: 저장 후 BASE_PLACE_SELECTING으로 전이
                    - BASE_PLACE_SELECTING 상태: 카테고리만 교체하고 상태 유지
                    """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<CourseDraftFoodCategorySaveResponse>> saveFoodCategories(
            @PathVariable Long courseDraftId,
            AuthMember authMember,
            CourseDraftFoodCategorySaveRequest request
    );
}
