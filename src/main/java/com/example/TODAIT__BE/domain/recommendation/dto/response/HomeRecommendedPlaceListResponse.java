package com.example.TODAIT__BE.domain.recommendation.dto.response;

import java.util.List;

public record HomeRecommendedPlaceListResponse(
        Long recommendationLogId,
        Integer page,
        Integer size,
        Boolean locationAvailable,
        List<HomeRecommendedPlaceResponse> places
) {
}
