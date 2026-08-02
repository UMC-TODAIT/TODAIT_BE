package com.example.TODAIT__BE.domain.recommendation.service.support;

import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.enums.CourseVisibility;
import com.example.TODAIT__BE.domain.course.repository.CoursePlaceRepository;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceReviewStatus;
import com.example.TODAIT__BE.domain.place.repository.PlaceFoodCategoryRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceMoodTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class HotPlaceCandidateLoader {

    private static final int CANDIDATE_LIMIT = 200;

    private final CoursePlaceRepository coursePlaceRepository;
    private final PlaceMoodTagRepository placeMoodTagRepository;
    private final PlaceFoodCategoryRepository placeFoodCategoryRepository;

    public HotPlaceCandidateData load() {
        List<Place> places =
                coursePlaceRepository.findHotPlaceCandidates(
                        CourseVisibility.RECOMMENDED,
                        CourseSourceType.SERVICE_CREATED,
                        PlaceReviewStatus.APPROVED,
                        PlaceExposureStatus.ACTIVE,
                        PageRequest.of(0, CANDIDATE_LIMIT)
                );

        if (places.isEmpty()) {
            return new HotPlaceCandidateData(
                    List.of(),
                    Map.of(),
                    Map.of()
            );
        }

        List<Long> placeIds =
                places.stream()
                        .map(Place::getId)
                        .toList();

        Map<Long, Set<Long>> moodTagIdsByPlaceId =
                loadMoodTagIdsByPlaceId(placeIds);

        Map<Long, Set<Long>> foodCategoryIdsByPlaceId =
                loadFoodCategoryIdsByPlaceId(placeIds);

        return new HotPlaceCandidateData(
                places,
                moodTagIdsByPlaceId,
                foodCategoryIdsByPlaceId
        );
    }

    private Map<Long, Set<Long>> loadMoodTagIdsByPlaceId(
            List<Long> placeIds
    ) {
        if (placeIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Set<Long>> moodTagIdsByPlaceId =
                new HashMap<>();

        for (PlaceMoodTagRepository.PlaceMoodTagIdView view
                : placeMoodTagRepository
                .findConfirmedMoodTagIdsByPlaceIds(placeIds)) {

            moodTagIdsByPlaceId
                    .computeIfAbsent(
                            view.getPlaceId(),
                            ignored -> new HashSet<>()
                    )
                    .add(view.getMoodTagId());
        }

        return moodTagIdsByPlaceId;
    }

    private Map<Long, Set<Long>> loadFoodCategoryIdsByPlaceId(
            List<Long> placeIds
    ) {
        if (placeIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Set<Long>> foodCategoryIdsByPlaceId =
                new HashMap<>();

        for (PlaceFoodCategoryRepository.PlaceFoodCategoryIdView view
                : placeFoodCategoryRepository
                .findFoodCategoryIdsByPlaceIds(placeIds)) {

            foodCategoryIdsByPlaceId
                    .computeIfAbsent(
                            view.getPlaceId(),
                            ignored -> new HashSet<>()
                    )
                    .add(view.getFoodCategoryId());
        }

        return foodCategoryIdsByPlaceId;
    }
}
