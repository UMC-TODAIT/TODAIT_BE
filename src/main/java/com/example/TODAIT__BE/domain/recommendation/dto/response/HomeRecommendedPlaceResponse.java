package com.example.TODAIT__BE.domain.recommendation.dto.response;

public record HomeRecommendedPlaceResponse(
        Long placeId,
        String name,
        String address,
        String roadAddress,
        Double latitude,
        Double longitude,
        RecommendedPlaceAreaResponse area,
        RecommendedPlaceCategoryResponse category,
        String subCategory,
        String imageUrl,
        Integer rank,
        Integer distanceMeters,
        Boolean isNearby,
        String recommendationReason,
        Boolean detailAvailable
) {
}
