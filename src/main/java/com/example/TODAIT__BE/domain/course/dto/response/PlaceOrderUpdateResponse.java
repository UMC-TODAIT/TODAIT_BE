package com.example.TODAIT__BE.domain.course.dto.response;

import java.util.List;

public record PlaceOrderUpdateResponse(
        Long courseDraftId,
        List<CourseDraftPlaceResponse> places
) {

    public static PlaceOrderUpdateResponse of(Long courseDraftId, List<CourseDraftPlaceResponse> places) {
        return new PlaceOrderUpdateResponse(courseDraftId, places);
    }
}
