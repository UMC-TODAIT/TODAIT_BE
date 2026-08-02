package com.example.TODAIT__BE.domain.recommendation.dto.response;

import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import java.util.List;

public class HotPlaceRecommendationResponse {

    private HotPlaceRecommendationResponse(){}

    public record Result(
            Long recommendationLogId,
            Long courseDraftId,
            CourseDraftStatus draftStatus,
            boolean locationAvailable,
            List<PlaceItem> places
    ) {}

    public record PlaceItem(
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
            int rank,
            Integer distanceMeters,
            Boolean isNearby,
            int matchedMoodCount,
            Integer matchedFoodCount,
            String recommendationReason,
            boolean detailAvailable
    ) {}

    public record AreaInfo(
            Long areaId,
            String code,
            String name
    ) {}

    public record CategoryInfo(
            Long placeCategoryId,
            String code,
            String name
    ) {}
}
