package com.example.TODAIT__BE.domain.recommendation.dto.response;

public record RecommendedPlaceCategoryResponse(
        Long placeCategoryId,
        String code,
        String name
) {
}
