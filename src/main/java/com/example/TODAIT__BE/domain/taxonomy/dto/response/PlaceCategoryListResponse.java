package com.example.TODAIT__BE.domain.taxonomy.dto.response;

import java.util.List;

public record PlaceCategoryListResponse(
        List<PlaceCategoryResponse> placeCategories
) {

    public record PlaceCategoryResponse(
            Long placeCategoryId,
            String code,
            String name,
            String description,
            Integer sortOrder
    ) {
    }
}
