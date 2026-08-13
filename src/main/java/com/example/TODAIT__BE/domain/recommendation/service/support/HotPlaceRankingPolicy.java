package com.example.TODAIT__BE.domain.recommendation.service.support;

import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.recommendation.service.support.HotPlaceCandidateLoader.CandidateData;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Component
public class HotPlaceRankingPolicy {

    private static final int NEARBY_DISTANCE_METERS = 500;
    private static final double EARTH_RADIUS_METERS = 6_371_000;
    private static final String ACTIVITY_CATEGORY_CODE = "ACTIVITY";

    public List<EvaluatedPlace> evaluateAndSort(
            CandidateData candidateData,
            Set<Long> selectedMoodTagIds,
            Set<Long> selectedFoodCategoryIds,
            Double latitude,
            Double longitude,
            boolean locationAvailable
    ) {
        Comparator<EvaluatedPlace> comparator =
                createComparator(locationAvailable);

        return candidateData.places().stream()
                        .map(place -> evaluate(
                                place,
                                candidateData,
                                selectedMoodTagIds,
                                selectedFoodCategoryIds,
                                latitude,
                                longitude,
                                locationAvailable
                        ))
                        .sorted(comparator)
                        .toList();
    }

    private Comparator<EvaluatedPlace> createComparator(
            boolean locationAvailable
    ) {
        Comparator<EvaluatedPlace> preferenceComparator =
                Comparator.comparingInt(
                                EvaluatedPlace::matchedMoodCount
                        )
                        .reversed()
                        .thenComparing(
                                this::foodMatchScore,
                                Comparator.reverseOrder()
                        );

        Comparator<EvaluatedPlace> stableComparator =
                Comparator.comparing(
                                (EvaluatedPlace evaluated) ->
                                        evaluated.place().getCreatedAt(),
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                        .thenComparing(
                                evaluated -> evaluated.place().getId()
                        );

        if (!locationAvailable) {
            return preferenceComparator.thenComparing(stableComparator);
        }

        return Comparator.comparing(
                        EvaluatedPlace::nearby,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
                .thenComparing(preferenceComparator)
                .thenComparing(
                        EvaluatedPlace::distanceMeters,
                        Comparator.nullsLast(
                                Comparator.naturalOrder()
                        )
                )
                .thenComparing(stableComparator);
    }

    private int foodMatchScore(EvaluatedPlace evaluated) {
        return evaluated.matchedFoodCount() == null
                ? 0
                : evaluated.matchedFoodCount();
    }

    private int countMatches(
            Set<Long> selectedIds,
            Set<Long> placeIds
    ) {
        if (selectedIds.isEmpty() || placeIds.isEmpty()) {
            return 0;
        }

        return (int) selectedIds.stream()
                .filter(placeIds::contains)
                .count();
    }

    private Integer calculateMatchedFoodCount(
            Place place,
            Set<Long> selectedFoodCategoryIds,
            Set<Long> placeFoodCategoryIds
    ) {
        if (isActivity(place)) {
            return null;
        }

        return countMatches(
                selectedFoodCategoryIds,
                placeFoodCategoryIds
        ) > 0 ? 1 : 0;
    }

    private boolean isActivity(Place place) {
        return place.getPlaceCategory() != null
                && ACTIVITY_CATEGORY_CODE.equals(
                        place.getPlaceCategory().getCode()
                );
    }

    // Haversine 공식을 통해 사용자와 후보 장소 간의 거리를 계산
    private int calculateDistanceMeters(
            double userLatitude,
            double userLongitude,
            double placeLatitude,
            double placeLongitude
    ) {
        double latitudeDistance =
                Math.toRadians(placeLatitude - userLatitude);

        double longitudeDistance =
                Math.toRadians(placeLongitude - userLongitude);

        double a =
                Math.sin(latitudeDistance / 2)
                        * Math.sin(latitudeDistance / 2)
                        + Math.cos(Math.toRadians(userLatitude))
                        * Math.cos(Math.toRadians(placeLatitude))
                        * Math.sin(longitudeDistance / 2)
                        * Math.sin(longitudeDistance / 2);

        double distance =
                EARTH_RADIUS_METERS
                        * 2
                        * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a)
                );

        return (int) Math.round(distance);
    }

    private EvaluatedPlace evaluate(
            Place place,
            CandidateData candidateData,
            Set<Long> selectedMoodTagIds,
            Set<Long> selectedFoodCategoryIds,
            Double latitude,
            Double longitude,
            boolean locationAvailable
    ) {
        Set<Long> placeMoodTagIds =
                candidateData.moodTagIdsByPlaceId()
                        .getOrDefault(place.getId(), Set.of());

        Set<Long> placeFoodCategoryIds =
                candidateData.foodCategoryIdsByPlaceId()
                        .getOrDefault(place.getId(), Set.of());

        int matchedMoodCount =
                countMatches(selectedMoodTagIds, placeMoodTagIds);

        Integer matchedFoodCount =
                calculateMatchedFoodCount(
                        place,
                        selectedFoodCategoryIds,
                        placeFoodCategoryIds
                );

        Integer distanceMeters = null;
        Boolean nearby = null;

        if (locationAvailable) {
            distanceMeters = calculateDistanceMeters(
                    latitude,
                    longitude,
                    place.getLatitude().doubleValue(),
                    place.getLongitude().doubleValue()
            );

            nearby = distanceMeters <= NEARBY_DISTANCE_METERS;
        }

        return new EvaluatedPlace(
                place,
                distanceMeters,
                nearby,
                matchedMoodCount,
                matchedFoodCount
        );
    }

    public record EvaluatedPlace(
            Place place,
            Integer distanceMeters,
            Boolean nearby,
            int matchedMoodCount,
            Integer matchedFoodCount
    ) {
    }
}
