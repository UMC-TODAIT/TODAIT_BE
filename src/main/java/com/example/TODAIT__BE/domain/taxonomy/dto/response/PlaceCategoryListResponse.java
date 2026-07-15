package com.example.TODAIT__BE.domain.taxonomy.dto.response;

import java.util.List;

public record PlaceCategoryListResponse(
        List<PlaceCategoryResponse> placeCategories
) {
}
