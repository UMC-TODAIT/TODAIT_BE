package com.example.TODAIT__BE.domain.course.dto.response;

public record SavedCoursePreviewPlaceResponse(
        Long placeId,
        String name,
        Integer visitOrder
) {
}
