package com.example.TODAIT__BE.domain.recommendation.service.support;

import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HotPlaceRankingPolicyTest {

    private final HotPlaceRankingPolicy rankingPolicy =
            new HotPlaceRankingPolicy();

    @Test
    void sortsByMoodAndFoodMatchesWhenLocationIsUnavailable() {
        Place preferenceMatch = place(
                1L,
                "취향 일치 장소",
                "RESTAURANT",
                37.5610,
                126.9230,
                LocalDateTime.of(2026, 8, 1, 10, 0)
        );

        Place noMatch = place(
                2L,
                "취향 불일치 장소",
                "RESTAURANT",
                37.5620,
                126.9240,
                LocalDateTime.of(2026, 8, 1, 9, 0)
        );

        HotPlaceCandidateData candidateData =
                new HotPlaceCandidateData(
                        List.of(noMatch, preferenceMatch),
                        Map.of(
                                1L, Set.of(10L, 11L),
                                2L, Set.of(99L)
                        ),
                        Map.of(
                                1L, Set.of(20L),
                                2L, Set.of(98L)
                        )
                );

        List<EvaluatedHotPlace> result =
                rankingPolicy.evaluateAndSort(
                        candidateData,
                        Set.of(10L, 11L),
                        Set.of(20L),
                        null,
                        null,
                        false
                );

        assertThat(result).extracting(
                evaluated -> evaluated.place().getId()
        ).containsExactly(1L, 2L);

        assertThat(result.get(0).matchedMoodCount()).isEqualTo(2);
        assertThat(result.get(0).matchedFoodCount()).isEqualTo(1);
        assertThat(result.get(0).distanceMeters()).isNull();
        assertThat(result.get(0).nearby()).isNull();
    }

    @Test
    void prioritizesNearbyPlaceWhenLocationIsAvailable() {
        Place nearby = place(
                1L,
                "가까운 장소",
                "RESTAURANT",
                37.5619,
                126.9230,
                LocalDateTime.of(2026, 8, 1, 10, 0)
        );

        Place farButPreferenceMatch = place(
                2L,
                "멀지만 취향 일치 장소",
                "RESTAURANT",
                37.5710,
                126.9230,
                LocalDateTime.of(2026, 8, 1, 9, 0)
        );

        HotPlaceCandidateData candidateData =
                new HotPlaceCandidateData(
                        List.of(farButPreferenceMatch, nearby),
                        Map.of(
                                1L, Set.of(),
                                2L, Set.of(10L, 11L)
                        ),
                        Map.of(
                                1L, Set.of(),
                                2L, Set.of(20L)
                        )
                );

        List<EvaluatedHotPlace> result =
                rankingPolicy.evaluateAndSort(
                        candidateData,
                        Set.of(10L, 11L),
                        Set.of(20L),
                        37.5610,
                        126.9230,
                        true
                );

        assertThat(result).extracting(
                evaluated -> evaluated.place().getId()
        ).containsExactly(1L, 2L);

        assertThat(result.get(0).nearby()).isTrue();
        assertThat(result.get(0).distanceMeters()).isLessThanOrEqualTo(500);
        assertThat(result.get(1).nearby()).isFalse();
        assertThat(result.get(1).distanceMeters()).isGreaterThan(500);
    }

    @Test
    void returnsNullFoodMatchForActivityPlace() {
        Place activity = place(
                1L,
                "방탈출 카페",
                "ACTIVITY",
                37.5610,
                126.9230,
                LocalDateTime.of(2026, 8, 1, 10, 0)
        );

        HotPlaceCandidateData candidateData =
                new HotPlaceCandidateData(
                        List.of(activity),
                        Map.of(1L, Set.of(10L)),
                        Map.of(1L, Set.of(20L))
                );

        EvaluatedHotPlace result =
                rankingPolicy.evaluateAndSort(
                        candidateData,
                        Set.of(10L),
                        Set.of(20L),
                        null,
                        null,
                        false
                ).get(0);

        assertThat(result.matchedMoodCount()).isEqualTo(1);
        assertThat(result.matchedFoodCount()).isNull();
    }

    private Place place(
            Long id,
            String name,
            String categoryCode,
            Double latitude,
            Double longitude,
            LocalDateTime createdAt
    ) {
        Place place = mock(Place.class);
        PlaceCategory category = mock(PlaceCategory.class);

        when(place.getId()).thenReturn(id);
        when(place.getName()).thenReturn(name);
        when(place.getLatitude()).thenReturn(latitude);
        when(place.getLongitude()).thenReturn(longitude);
        when(place.getCreatedAt()).thenReturn(createdAt);
        when(place.getPlaceCategory()).thenReturn(category);
        when(category.getCode()).thenReturn(categoryCode);

        return place;
    }
}
