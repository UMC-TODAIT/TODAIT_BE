package com.example.TODAIT__BE.domain.recommendation.service.support;

import com.example.TODAIT__BE.domain.place.entity.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class NearBasePlaceRankingPolicy {

    private static final String CATEGORY_RESTAURANT = "RESTAURANT";
    private static final String CATEGORY_CAFE = "CAFE";

    private static final int DISTANCE_500_METERS = 500;
    private static final int DISTANCE_1_KM = 1_000;
    private static final int DISTANCE_2_KM = 2_000;

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    public NearBasePlaceRecommendationSelection evaluateAndSelect(
            NearBasePlaceCandidateData candidateData,
            Place basePlace,
            Set<Long> selectedMoodTagIds,
            Set<Long> selectedFoodCategoryIds,
            Long dessertFoodCategoryId,
            String placeCategoryCode,
            int size
    ) {
        List<EvaluatedNearBasePlace> evaluated =
                evaluate(
                        candidateData,
                        basePlace,
                        selectedMoodTagIds,
                        selectedFoodCategoryIds,
                        dessertFoodCategoryId,
                        placeCategoryCode
                );

        evaluated.sort(recommendationComparator());

        return applyRelaxation(evaluated, size);
    }

    private List<EvaluatedNearBasePlace> evaluate(
            NearBasePlaceCandidateData candidateData,
            Place basePlace,
            Set<Long> selectedMoodTagIds,
            Set<Long> selectedFoodCategoryIds,
            Long dessertFoodCategoryId,
            String placeCategoryCode
    ) {
        List<EvaluatedNearBasePlace> result = new ArrayList<>();

        for (Place place : candidateData.places()) {

            int distanceMeters = calculateDistanceMeters(
                    basePlace.getLatitude(),
                    basePlace.getLongitude(),
                    place.getLatitude(),
                    place.getLongitude()
            );

            if (distanceMeters > DISTANCE_2_KM) {
                continue;
            }

            Set<Long> placeMoodTagIds =
                    candidateData.moodTagIdsByPlaceId()
                            .getOrDefault(place.getId(), Set.of());

            Set<Long> matchedMoodTagIds =
                    intersection(
                            selectedMoodTagIds,
                            placeMoodTagIds
                    );

            int matchedMoodCount =
                    Math.min(matchedMoodTagIds.size(), 2);

            int moodScore =
                    calculateMoodScore(matchedMoodCount);

            Set<Long> placeFoodCategoryIds =
                    candidateData.foodCategoryIdsByPlaceId()
                            .getOrDefault(place.getId(), Set.of());

            Integer matchedFoodCount =
                    calculateMatchedFoodCount(
                            placeCategoryCode,
                            selectedFoodCategoryIds,
                            placeFoodCategoryIds,
                            dessertFoodCategoryId
                    );

            int foodScore =
                    matchedFoodCount != null
                            && matchedFoodCount > 0
                            ? 3
                            : 0;

            int distanceScore =
                    calculateDistanceScore(distanceMeters);

            boolean sameArea =
                    basePlace.getArea()
                            .getCode()
                            .equals(place.getArea().getCode());

            int areaScore = sameArea ? 2 : 1;

            int internalScore =
                    moodScore
                            + foodScore
                            + distanceScore
                            + areaScore;

            result.add(
                    new EvaluatedNearBasePlace(
                            place,
                            distanceMeters,
                            matchedMoodCount,
                            matchedMoodTagIds,
                            matchedFoodCount,
                            internalScore,
                            sameArea
                    )
            );
        }

        return result;
    }

    private Integer calculateMatchedFoodCount(
            String placeCategoryCode,
            Set<Long> selectedFoodCategoryIds,
            Set<Long> placeFoodCategoryIds,
            Long dessertFoodCategoryId
    ) {
        if (CATEGORY_RESTAURANT.equals(placeCategoryCode)) {
            return hasIntersection(
                    selectedFoodCategoryIds,
                    placeFoodCategoryIds
            ) ? 1 : 0;
        }

        if (CATEGORY_CAFE.equals(placeCategoryCode)) {
            boolean userSelectedDessert =
                    selectedFoodCategoryIds.contains(
                            dessertFoodCategoryId
                    );

            boolean placeIsDessertCafe =
                    placeFoodCategoryIds.contains(
                            dessertFoodCategoryId
                    );

            return userSelectedDessert && placeIsDessertCafe
                    ? 1
                    : 0;
        }

        return null;
    }

    private int calculateMoodScore(int matchedMoodCount) {
        if (matchedMoodCount >= 2) {
            return 6;
        }

        if (matchedMoodCount == 1) {
            return 3;
        }

        return 0;
    }

    private int calculateDistanceScore(int distanceMeters) {
        if (distanceMeters <= DISTANCE_500_METERS) {
            return 3;
        }

        if (distanceMeters <= DISTANCE_1_KM) {
            return 2;
        }

        return 1;
    }

    private NearBasePlaceRecommendationSelection applyRelaxation(
            List<EvaluatedNearBasePlace> sortedPlaces,
            int size
    ) {
        List<EvaluatedNearBasePlace> selected = new ArrayList<>();
        Set<Long> selectedPlaceIds = new HashSet<>();

        int appliedLevel = 1;

        addMatchingPlaces(
                sortedPlaces,
                selected,
                selectedPlaceIds,
                size,
                place ->
                        place.matchedMoodCount() >= 1
                                && foodConditionSatisfied(place)
                                && place.distanceMeters() <= DISTANCE_1_KM
        );

        if (selected.size() < size) {
            appliedLevel = 2;

            addMatchingPlaces(
                    sortedPlaces,
                    selected,
                    selectedPlaceIds,
                    size,
                    place ->
                            place.matchedMoodCount() >= 1
                                    && place.distanceMeters() <= DISTANCE_1_KM
            );
        }

        if (selected.size() < size) {
            appliedLevel = 3;

            addMatchingPlaces(
                    sortedPlaces,
                    selected,
                    selectedPlaceIds,
                    size,
                    place ->
                            place.matchedMoodCount() >= 1
                                    && place.distanceMeters() <= DISTANCE_2_KM
            );
        }

        if (selected.size() < size) {
            appliedLevel = 4;

            addMatchingPlaces(
                    sortedPlaces,
                    selected,
                    selectedPlaceIds,
                    size,
                    place ->
                            place.distanceMeters() <= DISTANCE_2_KM
            );
        }

        return new NearBasePlaceRecommendationSelection(
                List.copyOf(selected),
                appliedLevel
        );
    }

    private boolean foodConditionSatisfied(
            EvaluatedNearBasePlace place
    ) {
        return place.matchedFoodCount() == null
                || place.matchedFoodCount() > 0;
    }

    private void addMatchingPlaces(
            List<EvaluatedNearBasePlace> source,
            List<EvaluatedNearBasePlace> selected,
            Set<Long> selectedPlaceIds,
            int size,
            java.util.function.Predicate<EvaluatedNearBasePlace> condition
    ) {
        for (EvaluatedNearBasePlace place : source) {

            if (selected.size() >= size) {
                return;
            }

            if (selectedPlaceIds.contains(
                    place.place().getId()
            )) {
                continue;
            }

            if (!condition.test(place)) {
                continue;
            }

            selected.add(place);
            selectedPlaceIds.add(
                    place.place().getId()
            );
        }
    }

    private Comparator<EvaluatedNearBasePlace>
    recommendationComparator() {

        return Comparator
                .comparingInt(
                        EvaluatedNearBasePlace::internalScore
                )
                .reversed()

                .thenComparing(
                        Comparator.comparingInt(
                                EvaluatedNearBasePlace
                                        ::matchedMoodCount
                        ).reversed()
                )

                .thenComparing(
                        place -> place.matchedFoodCount() == null
                                ? -1
                                : place.matchedFoodCount(),
                        Comparator.reverseOrder()
                )

                .thenComparingInt(
                        EvaluatedNearBasePlace::distanceMeters
                )

                .thenComparing(
                        EvaluatedNearBasePlace::sameArea,
                        Comparator.reverseOrder()
                )

                .thenComparing(
                        place -> place.place().getName()
                )

                .thenComparing(
                        place -> place.place().getId()
                );
    }

    private Set<Long> intersection(
            Set<Long> left,
            Set<Long> right
    ) {
        Set<Long> result = new HashSet<>(left);
        result.retainAll(right);
        return result;
    }

    private boolean hasIntersection(
            Set<Long> left,
            Set<Long> right
    ) {
        for (Long id : left) {
            if (right.contains(id)) {
                return true;
            }
        }

        return false;
    }

    private int calculateDistanceMeters(
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2
    ) {
        double latitudeDistance =
                Math.toRadians(latitude2 - latitude1);

        double longitudeDistance =
                Math.toRadians(longitude2 - longitude1);

        double a =
                Math.sin(latitudeDistance / 2)
                        * Math.sin(latitudeDistance / 2)
                        + Math.cos(Math.toRadians(latitude1))
                        * Math.cos(Math.toRadians(latitude2))
                        * Math.sin(longitudeDistance / 2)
                        * Math.sin(longitudeDistance / 2);

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a)
                );

        return (int) Math.round(
                EARTH_RADIUS_METERS * c
        );
    }
}
