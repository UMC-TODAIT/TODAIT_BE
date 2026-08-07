package com.example.TODAIT__BE.domain.course.dto.request;

import jakarta.validation.constraints.NotNull;

public record CourseDraftPlaceAddRequest(
        @NotNull
        Long placeId
) {
}
