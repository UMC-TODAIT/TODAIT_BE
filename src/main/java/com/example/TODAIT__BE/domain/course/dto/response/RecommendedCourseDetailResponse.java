package com.example.TODAIT__BE.domain.course.dto.response;

import java.util.List;

public record RecommendedCourseDetailResponse(
        Long courseId,
        String title,
        RepresentativeMoodTagResponse representativeMoodTag,
        RepresentativeSubCategoryResponse representativePlaceCategory,
        Integer placeCount,
        List<RecommendedCoursePlaceResponse> places
) {
}
