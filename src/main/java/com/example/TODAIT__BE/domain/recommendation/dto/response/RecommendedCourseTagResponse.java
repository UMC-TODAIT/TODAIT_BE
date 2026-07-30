package com.example.TODAIT__BE.domain.recommendation.dto.response;

public record RecommendedCourseTagResponse(
        String type,
        String code,
        String name
) {
    public static RecommendedCourseTagResponse mood(String code, String name) {
        return new RecommendedCourseTagResponse("MOOD", code, name);
    }

    public static RecommendedCourseTagResponse subCategory(String name) {
        return new RecommendedCourseTagResponse("SUB_CATEGORY", null, name);
    }
}
