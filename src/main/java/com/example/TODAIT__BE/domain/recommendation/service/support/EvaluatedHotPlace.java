package com.example.TODAIT__BE.domain.recommendation.service.support;

import com.example.TODAIT__BE.domain.place.entity.Place;

public record EvaluatedHotPlace(
        Place place,
        Integer distanceMeters,
        Boolean nearby,
        int matchedMoodCount,
        Integer matchedFoodCount
) {
}
