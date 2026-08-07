package com.example.TODAIT__BE.domain.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record CategoryRecommendedPlaceResponse(

        @Schema(description = "추천 로그 ID", example = "101")
        Long recommendationLogId,

        @Schema(description = "임시 코스 ID", example = "12")
        Long courseDraftId,

        @Schema(description = "현재 임시 코스 상태", example = "PLACE_SELECTING")
        String draftStatus,

        PlaceCategoryInfo placeCategory,

        BasePlaceInfo basePlace,

        @Schema(description = "적용된 조건 완화 단계", example = "2")
        Integer appliedRelaxationLevel,

        List<PlaceInfo> places
) {

    public record PlaceCategoryInfo(
            @Schema(description = "장소 카테고리 ID", example = "1")
            Long placeCategoryId,

            @Schema(description = "장소 카테고리 코드", example = "CAFE")
            String code,

            @Schema(description = "장소 카테고리 표시명", example = "카페")
            String name
    ) {
    }

    public record BasePlaceInfo(
            Long placeId,
            String name,
            AreaInfo area
    ) {
    }

    public record AreaInfo(
            Long areaId,
            String code,
            String name
    ) {
    }

    public record CategoryInfo(
            Long placeCategoryId,
            String code,
            String name
    ) {
    }

    public record MoodTagInfo(
            @Schema(description = "분위기 태그 ID", example = "3")
            Long moodTagId,

            @Schema(description = "분위기 태그 코드", example = "ROMANTIC")
            String code,

            @Schema(description = "분위기 태그 표시명", example = "로맨틱")
            String name
    ) {
    }

    public record PlaceInfo(
            Long placeId,
            String name,
            String address,
            String roadAddress,
            Double latitude,
            Double longitude,

            AreaInfo area,
            CategoryInfo category,

            String subCategory,
            String imageUrl,

            Integer rank,
            Integer distanceMeters,

            Integer matchedMoodCount,
            List<MoodTagInfo> matchedMoodTags,

            Integer matchedFoodCount,
            Integer internalScore,

            List<String> recommendationReasons,

            Boolean alreadySelected,
            Boolean detailAvailable
    ) {
    }
}
