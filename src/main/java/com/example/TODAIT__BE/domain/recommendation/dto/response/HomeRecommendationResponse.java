package com.example.TODAIT__BE.domain.recommendation.dto.response;

import java.util.List;

public final class HomeRecommendationResponse {

    private HomeRecommendationResponse() {
    }

    public record CourseList(
            Long recommendationLogId,
            int size,
            boolean hasNext,
            String nextCursor,
            List<CourseItem> courses
    ) {
    }

    public record CourseItem(
            Long courseId,
            String title,
            CourseArea area,
            String representativeImageUrl,
            List<CourseTag> tags,
            int placeCount,
            int rank,
            boolean detailAvailable
    ) {
    }

    public record CourseArea(
            Long areaId,
            String code,
            String name
    ) {
    }

    public record CourseTag(
            String type,
            String code,
            String name
    ) {
        public static CourseTag mood(String code, String name) {
            return new CourseTag("MOOD", code, name);
        }

        public static CourseTag subCategory(String name) {
            return new CourseTag("SUB_CATEGORY", null, name);
        }
    }

    public record PlaceList(
            Long recommendationLogId,
            Integer size,
            Boolean locationAvailable,
            Boolean hasNext,
            String nextCursor,
            List<PlaceItem> places
    ) {
    }

    public record PlaceItem(
            Long placeId,
            String name,
            String address,
            String roadAddress,
            Double latitude,
            Double longitude,
            PlaceArea area,
            PlaceCategory category,
            String subCategory,
            String imageUrl,
            Integer rank,
            Integer distanceMeters,
            Boolean isNearby,
            String recommendationReason,
            Boolean detailAvailable
    ) {
    }

    public record PlaceArea(
            Long areaId,
            String code,
            String name
    ) {
    }

    public record PlaceCategory(
            Long placeCategoryId,
            String code,
            String name
    ) {
    }
}
