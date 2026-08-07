package com.example.TODAIT__BE.domain.recommendation.service.support;

import java.util.List;

public record NearBasePlaceRecommendationSelection(
        List<EvaluatedNearBasePlace> places,
        int appliedRelaxationLevel
) {
}
