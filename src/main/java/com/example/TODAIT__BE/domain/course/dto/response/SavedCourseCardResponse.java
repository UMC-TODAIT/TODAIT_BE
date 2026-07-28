package com.example.TODAIT__BE.domain.course.dto.response;

import java.time.LocalDate;
import java.util.List;

public record SavedCourseCardResponse(
        Long courseId,
        String title,
        LocalDate savedDate,
        RepresentativeMoodTagResponse representativeMoodTag,
        RepresentativeSubCategoryResponse representativePlaceCategory,
        List<SavedCoursePreviewPlaceResponse> previewPlaces,
        Integer remainingPlaceCount,
        Integer placeCount,
        Integer viewCount
) {
}
