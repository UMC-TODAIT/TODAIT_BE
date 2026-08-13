package com.example.TODAIT__BE.domain.recommendation.controller.docs;

import com.example.TODAIT__BE.domain.recommendation.dto.response.CategoryRecommendedPlaceResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "RECOMMENDATION",
        description = "추천 코스 및 추천 장소 API"
)
public interface CategoryRecommendedPlaceControllerDocs {

    @Operation(
            summary = "[코스 구성] 카테고리별 추천 장소 목록 조회",
            description = """
                    임시 코스의 기준 장소와 사용자가 선택한 취향 정보를 기반으로
                    특정 장소 카테고리의 추천 장소 목록을 조회합니다.

                    - 호출 가능 상태: PLACE_SELECTING
                    - 지원 카테고리: CAFE, ACTIVITY, RESTAURANT, BAR, OTHER
                    - size 기본값: 10
                    - size 허용 범위: 1~20
                    - 기준 장소, 분위기 태그, 음식 카테고리는 임시 코스에 저장된 값을 사용합니다.
                    - 이미 임시 코스에 추가된 장소와 기준 장소는 추천 대상에서 제외됩니다.
                    - 추천 결과가 부족한 경우 음식 → 거리 → 분위기 순으로 조건을 완화합니다.
                    """
    )
    @SecurityRequirement(name = "JWT TOKEN")
    ResponseEntity<ApiResponse<CategoryRecommendedPlaceResponse>>
    getCategoryRecommendedPlaces(
            @Parameter(hidden = true)
            AuthMember authMember,

            @Parameter(
                    description = "추천 장소를 조회할 임시 코스 ID",
                    example = "12",
                    required = true
            )
            Long courseDraftId,

            @Parameter(
                    description = "조회할 장소 카테고리 코드 (CAFE, ACTIVITY, RESTAURANT, BAR, OTHER)",
                    example = "OTHER",
                    required = true
            )
            String placeCategoryCode,

            @Parameter(
                    description = "조회할 추천 장소 개수. 생략 시 10, 허용 범위 1~20",
                    example = "10"
            )
            Integer size
    );
}
