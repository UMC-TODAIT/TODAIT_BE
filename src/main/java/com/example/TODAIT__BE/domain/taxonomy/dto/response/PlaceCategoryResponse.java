package com.example.TODAIT__BE.domain.taxonomy.dto.response;

public record PlaceCategoryResponse(
        Long placeCategoryId,
        String code,
        String name,
        String description,
        Integer sortOrder
) {
}
