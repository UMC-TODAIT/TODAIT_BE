package com.example.TODAIT__BE.domain.recommendation.service.support;

import com.example.TODAIT__BE.domain.place.entity.Place;

import java.util.Set;

public record EvaluatedNearBasePlace(
        Place place,
        int distanceMeters,
        int matchedMoodCount,
        Set<Long> matchedMoodTagIds,
        Integer matchedFoodCount,
        int internalScore,
        boolean sameArea
) {
}
