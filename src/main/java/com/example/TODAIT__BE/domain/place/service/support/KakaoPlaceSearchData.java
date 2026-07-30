package com.example.TODAIT__BE.domain.place.service.support;

import com.example.TODAIT__BE.domain.place.entity.Place;

import java.util.Map;
import java.util.Set;

public record KakaoPlaceSearchData(
        Map<String, Place> registeredPlacesByExternalId,
        Map<Long, String> primaryImageUrlsByPlaceId,
        Set<Long> operatorSourcePlaceIds
) {
}
