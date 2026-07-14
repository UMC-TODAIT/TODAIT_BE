package com.example.TODAIT__BE.domain.place.dto.response;

import com.example.TODAIT__BE.domain.place.entity.Place;
import java.util.List;

public record PlaceResponse(
        Long placeId,
        String name,
        String address,
        String roadAddress,
        Double latitude,
        Double longitude,
        String defaultImageUrl,
        PlaceCategorySummary placeCategory,
        FoodCategorySummary primaryFoodCategory,
        List<MoodTagSummary> moodTags
) {

    public static PlaceResponse from(Place place) {
        PlaceCategorySummary placeCategory = place.getPlaceCategory() != null
                ? new PlaceCategorySummary(place.getPlaceCategory().getId(), place.getPlaceCategory().getName())
                : null;

        FoodCategorySummary primaryFoodCategory = place.getPrimaryFoodCategory() != null
                ? new FoodCategorySummary(place.getPrimaryFoodCategory().getId(), place.getPrimaryFoodCategory().getName())
                : null;

        List<MoodTagSummary> moodTags = place.getMoodTags().stream()
                .map(moodTag -> new MoodTagSummary(moodTag.getId(), moodTag.getName()))
                .toList();

        return new PlaceResponse(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getRoadAddress(),
                place.getLatitude(),
                place.getLongitude(),
                place.getDefaultImageUrl(),
                placeCategory,
                primaryFoodCategory,
                moodTags
        );
    }
}
