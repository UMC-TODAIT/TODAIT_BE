package com.example.TODAIT__BE.domain.recommendation.service.support;

import com.example.TODAIT__BE.domain.place.entity.Place;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record HotPlaceCandidateData(
        List<Place> places,
        Map<Long, Set<Long>> moodTagIdsByPlaceId,
        Map<Long, Set<Long>> foodCategoryIdsByPlaceId
) {
}
