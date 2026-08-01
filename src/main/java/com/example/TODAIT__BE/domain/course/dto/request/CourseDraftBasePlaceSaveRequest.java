package com.example.TODAIT__BE.domain.course.dto.request;

public record CourseDraftBasePlaceSaveRequest(
        Long placeId,
        ExternalPlace externalPlace
) {

    public record ExternalPlace(
            String dataSourceCode,
            String sourcePlaceId,
            String name,
            String address,
            String roadAddress,
            Double latitude,
            Double longitude,
            String areaCode,
            String categoryCode,
            String subCategory,
            String phone,
            String sourceUrl
    ) {
    }
}
