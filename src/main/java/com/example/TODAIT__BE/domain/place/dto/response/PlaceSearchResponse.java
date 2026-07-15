package com.example.TODAIT__BE.domain.place.dto.response;

import java.util.List;

public record PlaceSearchResponse(
        List<PlaceResponse> places
) {
}
