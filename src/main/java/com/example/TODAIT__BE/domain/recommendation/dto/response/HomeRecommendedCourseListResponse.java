package com.example.TODAIT__BE.domain.recommendation.dto.response;

import java.util.List;

public record HomeRecommendedCourseListResponse(
        Long recommendationLogId,
        int size,
        boolean hasNext,
        String nextCursor,
        List<HomeRecommendedCourseResponse> courses
) {
}
