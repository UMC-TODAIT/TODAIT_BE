package com.example.TODAIT__BE.domain.recommendation.dto.response;

import java.util.List;

public record HomeRecommendedCourseResponse(
        Long courseId,
        String title,
        RecommendedCourseAreaResponse area,
        String representativeImageUrl,
        List<RecommendedCourseTagResponse> tags,
        int placeCount,
        int rank,
        boolean detailAvailable
) {
}
