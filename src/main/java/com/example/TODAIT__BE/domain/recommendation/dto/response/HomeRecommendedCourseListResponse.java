package com.example.TODAIT__BE.domain.recommendation.dto.response;

import java.util.List;

public record HomeRecommendedCourseListResponse(
        Long recommendationLogId,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        List<HomeRecommendedCourseResponse> courses
) {
}
