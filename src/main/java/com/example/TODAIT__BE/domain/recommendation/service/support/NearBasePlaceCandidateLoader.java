package com.example.TODAIT__BE.domain.recommendation.service.support;

import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceReviewStatus;
import com.example.TODAIT__BE.domain.place.repository.PlaceFoodCategoryRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceMoodTagRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class NearBasePlaceCandidateLoader {

    private static final String OPERATOR_SOURCE_CODE = "OPERATOR";

    private final PlaceRepository placeRepository;
    private final PlaceSourceRepository placeSourceRepository;
    private final PlaceMoodTagRepository placeMoodTagRepository;
    private final PlaceFoodCategoryRepository placeFoodCategoryRepository;

    public CandidateData load(
            List<String> areaCodes,
            String placeCategoryCode,
            Set<Long> excludedPlaceIds
    ) {
        List<Place> rawCandidates =
                placeRepository.findNearBasePlaceRecommendationCandidates(
                        PlaceReviewStatus.APPROVED,
                        PlaceExposureStatus.ACTIVE,
                        areaCodes,
                        placeCategoryCode
                );

        if (rawCandidates.isEmpty()) {
            return emptyData();
        }

        List<Long> rawCandidateIds = rawCandidates.stream()
                .map(Place::getId)
                .toList();

        Set<Long> sourcedPlaceIds =
                placeSourceRepository.findPlaceIdsHavingActiveSource(
                        rawCandidateIds
                );

        Set<Long> operatorPlaceIds =
                placeSourceRepository.findPlaceIdsHavingActiveDataSource(
                        rawCandidateIds,
                        OPERATOR_SOURCE_CODE
                );

        List<Place> candidates = rawCandidates.stream()
                .filter(place ->
                        isOperatorManagedPlace(
                                place.getId(),
                                sourcedPlaceIds,
                                operatorPlaceIds
                        )
                )
                .filter(place ->
                        !excludedPlaceIds.contains(place.getId())
                )
                .toList();

        if (candidates.isEmpty()) {
            return emptyData();
        }

        List<Long> candidateIds = candidates.stream()
                .map(Place::getId)
                .toList();

        Map<Long, Set<Long>> moodTagIdsByPlaceId =
                loadMoodTagIds(candidateIds);

        Map<Long, Set<Long>> foodCategoryIdsByPlaceId =
                loadFoodCategoryIds(candidateIds);

        return new CandidateData(
                candidates,
                moodTagIdsByPlaceId,
                foodCategoryIdsByPlaceId
        );
    }

    private boolean isOperatorManagedPlace(
            Long placeId,
            Set<Long> sourcedPlaceIds,
            Set<Long> operatorPlaceIds
    ) {
        return !sourcedPlaceIds.contains(placeId)
                || operatorPlaceIds.contains(placeId);
    }

    private Map<Long, Set<Long>> loadMoodTagIds(
            List<Long> placeIds
    ) {
        Map<Long, Set<Long>> result = new HashMap<>();

        for (PlaceMoodTagRepository.PlaceMoodTagIdView view
                : placeMoodTagRepository
                .findConfirmedMoodTagIdsByPlaceIds(placeIds)) {

            result.computeIfAbsent(
                    view.getPlaceId(),
                    ignored -> new HashSet<>()
            ).add(view.getMoodTagId());
        }

        return result;
    }

    private Map<Long, Set<Long>> loadFoodCategoryIds(
            List<Long> placeIds
    ) {
        Map<Long, Set<Long>> result = new HashMap<>();

        for (PlaceFoodCategoryRepository.PlaceFoodCategoryIdView view
                : placeFoodCategoryRepository
                .findFoodCategoryIdsByPlaceIds(placeIds)) {

            result.computeIfAbsent(
                    view.getPlaceId(),
                    ignored -> new HashSet<>()
            ).add(view.getFoodCategoryId());
        }

        return result;
    }

    private CandidateData emptyData() {
        return new CandidateData(
                List.of(),
                Map.of(),
                Map.of()
        );
    }

    public record CandidateData(
            List<Place> places,
            Map<Long, Set<Long>> moodTagIdsByPlaceId,
            Map<Long, Set<Long>> foodCategoryIdsByPlaceId
    ) {
    }
}
