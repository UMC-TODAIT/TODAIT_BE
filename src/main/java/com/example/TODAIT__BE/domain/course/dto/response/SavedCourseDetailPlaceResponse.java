package com.example.TODAIT__BE.domain.course.dto.response;

public record SavedCourseDetailPlaceResponse(
        Long coursePlaceId,
        Long placeId,
        Integer visitOrder,
        String name,
        String address,
        String memo
) {
}
