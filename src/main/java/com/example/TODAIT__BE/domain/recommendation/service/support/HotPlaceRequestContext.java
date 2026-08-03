package com.example.TODAIT__BE.domain.recommendation.service.support;

import java.util.Set;

public record HotPlaceRequestContext(
        boolean locationAvailable,
        int nearbyDistanceMeters,
        Set<Long> selectedMoodTagIds,
        Set<Long> selectedFoodCategoryIds,
        int limit,
        String policyVersion
) {
    public HotPlaceRequestContext {
        selectedMoodTagIds = Set.copyOf(selectedMoodTagIds);
        selectedFoodCategoryIds =
                Set.copyOf(selectedFoodCategoryIds);
    }

}
