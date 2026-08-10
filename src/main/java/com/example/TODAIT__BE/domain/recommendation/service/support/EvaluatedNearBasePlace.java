package com.example.TODAIT__BE.domain.recommendation.service.support;

import com.example.TODAIT__BE.domain.place.entity.Place;

import java.util.ArrayList;
import java.util.List;
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
    private static final int MAX_REASON_COUNT = 2;

    public List<String> recommendationReasons() {
        List<String> reasons = new ArrayList<>();

        addMoodReason(reasons);
        addFoodReason(reasons);
        addDistanceReason(reasons);
        addAreaReason(reasons);

        return reasons.stream()
                .limit(MAX_REASON_COUNT)
                .toList();
    }

    private void addMoodReason(List<String> reasons) {
        if (matchedMoodCount >= 2) {
            reasons.add("선택한 분위기와 특히 잘 어울려요.");
            return;
        }

        if (matchedMoodCount == 1) {
            reasons.add("선택한 분위기와 잘 어울려요.");
        }
    }

    private void addFoodReason(List<String> reasons) {
        if (matchedFoodCount != null && matchedFoodCount > 0) {
            reasons.add("선택한 음식 취향과 잘 맞아요.");
        }
    }

    private void addDistanceReason(List<String> reasons) {
        if (distanceMeters <= 500) {
            reasons.add("기준 장소 바로 근처에 있어요.");
            return;
        }

        if (distanceMeters <= 1_000) {
            reasons.add("기준 장소에서 가까운 곳에 있어요.");
            return;
        }

        if (distanceMeters <= 2_000) {
            reasons.add("기준 장소에서 이동하기 좋은 거리에 있어요.");
        }
    }

    private void addAreaReason(List<String> reasons) {
        if (sameArea) {
            reasons.add("기준 장소와 같은 지역에 있어요.");
        } else {
            reasons.add("기준 장소와 함께 둘러보기 좋은 지역에 있어요.");
        }
    }
}
