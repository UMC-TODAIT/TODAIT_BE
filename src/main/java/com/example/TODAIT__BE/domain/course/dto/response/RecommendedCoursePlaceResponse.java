package com.example.TODAIT__BE.domain.course.dto.response;

public record RecommendedCoursePlaceResponse(
        Long coursePlaceId,
        Long placeId,
        Integer visitOrder,
        String name,
        String representativeImageUrl,
        String address,
        Double latitude,
        Double longitude
) {
}
