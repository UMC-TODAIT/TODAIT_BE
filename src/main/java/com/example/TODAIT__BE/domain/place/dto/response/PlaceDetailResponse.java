package com.example.TODAIT__BE.domain.place.dto.response;

import com.example.TODAIT__BE.domain.place.enums.BusinessStatus;
import java.util.List;

public record PlaceDetailResponse(
        Long placeId,
        String name,
        String address,
        String roadAddress,
        Double latitude,
        Double longitude,
        String phone,
        String subCategory,
        String defaultImageUrl,
        BusinessStatus businessStatus,
        String lastOrderTime,
        PlaceDetailCategoryResponse placeCategory,
        PlaceDetailFoodCategoryResponse primaryFoodCategory,
        List<PlaceDetailFoodCategoryResponse> foodCategories,
        List<PlaceDetailMoodTagResponse> moodTags,
        List<String> imageUrls,
        List<String> interiorImageUrls,
        List<PlaceMenuResponse> menus,
        String defaultRecommendReason
) {
}
