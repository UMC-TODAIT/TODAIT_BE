package com.example.TODAIT__BE.domain.place.dto.response;

public record PlaceMenuResponse(
        Long placeMenuId,
        String name,
        Integer price,
        String imageUrl
) {
}
