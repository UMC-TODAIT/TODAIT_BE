package com.example.TODAIT__BE.domain.recommendation.dto.response;

import java.util.List;

public record HomeRecommendedPlaceListResponse(
        Long recommendationLogId,
        Integer size,
        Boolean locationAvailable,
        Boolean hasNext,
        String nextCursor,
        List<HomeRecommendedPlaceResponse> places
) {
}
