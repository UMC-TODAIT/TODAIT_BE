package com.example.TODAIT__BE.domain.recommendation.service.support;

import com.example.TODAIT__BE.domain.place.entity.Place;
import org.springframework.stereotype.Component;

@Component
public class HotPlaceRecommendationReasonResolver {

    private static final String NEARBY_REASON =
            "현재 위치와 가까워요.";

    private static final String MOOD_REASON =
            "선택한 분위기와 잘 어울려요.";

    private static final String FOOD_REASON =
            "원하는 음식 취향과 잘 맞아요.";

    public String resolve(
            EvaluatedHotPlace evaluated,
            boolean locationAvailable
    ) {
        if (locationAvailable
                && Boolean.TRUE.equals(evaluated.nearby())) {
            return NEARBY_REASON;
        }

        if (evaluated.matchedMoodCount() > 0) {
            return MOOD_REASON;
        }

        if (evaluated.matchedFoodCount() != null
                && evaluated.matchedFoodCount() > 0) {
            return FOOD_REASON;
        }

        return createAreaReason(evaluated.place());
    }

    private String createAreaReason(Place place) {
        if (place.getArea() != null
                && place.getArea().getName() != null
                && !place.getArea().getName().isBlank()) {
            return place.getArea().getName()
                    + " 추천 장소예요.";
        }

        if (place.getDefaultRecommendReason() != null
                && !place.getDefaultRecommendReason().isBlank()) {
            return place.getDefaultRecommendReason();
        }

        return "지금 가기 좋은 추천 장소예요.";
    }
}
