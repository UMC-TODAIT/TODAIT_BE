package com.example.TODAIT__BE.domain.recommendation.controller.docs;

import com.example.TODAIT__BE.domain.recommendation.dto.response.HotPlaceRecommendationResponse;
import com.example.TODAIT__BE.global.apiPayload.ApiResponse;
import com.example.TODAIT__BE.global.security.principal.AuthMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "RECOMMENDATION",
        description = "추천 도메인 API"
)
public interface HotPlaceRecommendationControllerDocs {

    @Operation(
            summary = "[임시 코스] 지금 내 주변 핫플 조회",
            description = """
                    기준 장소 설정 화면에 표시할 추천 장소 목록을 조회합니다.
                    현재 임시 코스에 저장된 분위기 및 음식 취향을 반영합니다.

                    latitude와 longitude가 모두 전달되면 위치 기반 추천을 적용하고,
                    둘 다 전달되지 않으면 취향 기반 추천을 적용합니다.

                    추천 요청과 최종 결과는 추천 이력으로 저장합니다.
                    """
    )
    ResponseEntity<
                ApiResponse<HotPlaceRecommendationResponse.Result>>
    getHotPlaces(
            @Parameter(hidden = true)
            AuthMember authMember,

            @Parameter(
                    description = "임시 코스 ID",
                    example = "15"
            )
            Long courseDraftId,

            @Parameter(
                    description = "사용자 현재 위도",
                    example = "37.561234"
            )
            Double latitude,

            @Parameter(
                    description = "사용자 현재 경도",
                    example = "126.923456"
            )
            Double longitude,

            @Parameter(
                    description = "반환할 장소 개수, 기본값 4, 범위 1~10",
                    example = "4"
            )
            Integer size
    );
}
