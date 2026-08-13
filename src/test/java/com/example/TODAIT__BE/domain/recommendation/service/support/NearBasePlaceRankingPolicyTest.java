package com.example.TODAIT__BE.domain.recommendation.service.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NearBasePlaceRankingPolicyTest {

    private static final Long DESSERT_CATEGORY_ID = 6L;

    private NearBasePlaceRankingPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new NearBasePlaceRankingPolicy();
    }

    @Test
    void selectsMoodAndFoodMatchedRestaurantAtRelaxationLevelOne() {
        Place basePlace = place(
                1L,
                "기준 장소",
                "HONGDAE",
                37.5563,
                126.9236
        );

        Place candidate = place(
                2L,
                "추천 음식점",
                "HONGDAE",
                37.5570,
                126.9236
        );

        NearBasePlaceCandidateLoader.CandidateData candidateData =
                new NearBasePlaceCandidateLoader.CandidateData(
                        List.of(candidate),
                        Map.of(
                                2L,
                                Set.of(10L, 20L)
                        ),
                        Map.of(
                                2L,
                                Set.of(100L)
                        )
                );

        NearBasePlaceRecommendationSelection result =
                policy.evaluateAndSelect(
                        candidateData,
                        basePlace,
                        Set.of(10L, 20L),
                        Set.of(100L),
                        DESSERT_CATEGORY_ID,
                        "RESTAURANT",
                        1
                );

        assertThat(result.appliedRelaxationLevel())
                .isEqualTo(1);

        assertThat(result.places())
                .hasSize(1);

        EvaluatedNearBasePlace selected =
                result.places().get(0);

        assertThat(selected.place().getId())
                .isEqualTo(2L);

        assertThat(selected.matchedMoodCount())
                .isEqualTo(2);

        assertThat(selected.matchedFoodCount())
                .isEqualTo(1);

        assertThat(selected.internalScore())
                .isEqualTo(14);
    }

    @Test
    void excludesPlaceFartherThanTwoKilometers() {
        Place basePlace = place(
                1L,
                "기준 장소",
                "HONGDAE",
                37.5563,
                126.9236
        );

        Place farPlace = place(
                2L,
                "너무 먼 장소",
                "HONGDAE",
                37.5800,
                126.9236
        );

        NearBasePlaceCandidateLoader.CandidateData candidateData =
                new NearBasePlaceCandidateLoader.CandidateData(
                        List.of(farPlace),
                        Map.of(
                                2L,
                                Set.of(10L)
                        ),
                        Map.of()
                );

        NearBasePlaceRecommendationSelection result =
                policy.evaluateAndSelect(
                        candidateData,
                        basePlace,
                        Set.of(10L),
                        Set.of(),
                        DESSERT_CATEGORY_ID,
                        "ACTIVITY",
                        10
                );

        assertThat(result.places())
                .isEmpty();
    }

    @Test
    void sortsHigherInternalScoreFirst() {
        Place basePlace = place(
                1L,
                "기준 장소",
                "HONGDAE",
                37.5563,
                126.9236
        );

        Place strongMoodMatch = place(
                2L,
                "분위기 강한 장소",
                "HONGDAE",
                37.5570,
                126.9236
        );

        Place weakMoodMatch = place(
                3L,
                "분위기 약한 장소",
                "HONGDAE",
                37.5570,
                126.9236
        );

        NearBasePlaceCandidateLoader.CandidateData candidateData =
                new NearBasePlaceCandidateLoader.CandidateData(
                        List.of(
                                weakMoodMatch,
                                strongMoodMatch
                        ),
                        Map.of(
                                2L,
                                Set.of(10L, 20L),
                                3L,
                                Set.of(10L)
                        ),
                        Map.of()
                );

        NearBasePlaceRecommendationSelection result =
                policy.evaluateAndSelect(
                        candidateData,
                        basePlace,
                        Set.of(10L, 20L),
                        Set.of(),
                        DESSERT_CATEGORY_ID,
                        "ACTIVITY",
                        2
                );

        assertThat(result.places())
                .extracting(place ->
                        place.place().getId()
                )
                .containsExactly(
                        2L,
                        3L
                );

        assertThat(result.places().get(0).internalScore())
                .isGreaterThan(
                        result.places()
                                .get(1)
                                .internalScore()
                );
    }

    @Test
    void relaxesToLevelFourWhenNoMoodMatches() {
        Place basePlace = place(
                1L,
                "기준 장소",
                "HONGDAE",
                37.5563,
                126.9236
        );

        Place candidate = place(
                2L,
                "분위기 불일치 장소",
                "HONGDAE",
                37.5570,
                126.9236
        );

        NearBasePlaceCandidateLoader.CandidateData candidateData =
                new NearBasePlaceCandidateLoader.CandidateData(
                        List.of(candidate),
                        Map.of(
                                2L,
                                Set.of(999L)
                        ),
                        Map.of()
                );

        NearBasePlaceRecommendationSelection result =
                policy.evaluateAndSelect(
                        candidateData,
                        basePlace,
                        Set.of(10L),
                        Set.of(),
                        DESSERT_CATEGORY_ID,
                        "ACTIVITY",
                        1
                );

        assertThat(result.appliedRelaxationLevel())
                .isEqualTo(4);

        assertThat(result.places())
                .hasSize(1);

        assertThat(result.places().get(0).matchedMoodCount())
                .isZero();
    }

    @Test
    void doesNotApplyFoodMatchingToActivityOrBar() {
        Place basePlace = place(
                1L,
                "기준 장소",
                "HONGDAE",
                37.5563,
                126.9236
        );

        Place candidate = place(
                2L,
                "액티비티 장소",
                "HONGDAE",
                37.5570,
                126.9236
        );

        NearBasePlaceCandidateLoader.CandidateData candidateData =
                new NearBasePlaceCandidateLoader.CandidateData(
                        List.of(candidate),
                        Map.of(
                                2L,
                                Set.of(10L)
                        ),
                        Map.of(
                                2L,
                                Set.of(100L)
                        )
                );

        NearBasePlaceRecommendationSelection activityResult =
                policy.evaluateAndSelect(
                        candidateData,
                        basePlace,
                        Set.of(10L),
                        Set.of(100L),
                        DESSERT_CATEGORY_ID,
                        "ACTIVITY",
                        1
                );

        NearBasePlaceRecommendationSelection barResult =
                policy.evaluateAndSelect(
                        candidateData,
                        basePlace,
                        Set.of(10L),
                        Set.of(100L),
                        DESSERT_CATEGORY_ID,
                        "BAR",
                        1
                );

        assertThat(
                activityResult.places()
                        .get(0)
                        .matchedFoodCount()
        ).isNull();

        assertThat(
                barResult.places()
                        .get(0)
                        .matchedFoodCount()
        ).isNull();
    }

    private Place place(
            Long id,
            String name,
            String areaCode,
            double latitude,
            double longitude
    ) {
        Area area = mock(Area.class);
        given(area.getCode())
                .willReturn(areaCode);

        Place place = mock(Place.class);
        given(place.getId())
                .willReturn(id);
        given(place.getName())
                .willReturn(name);
        given(place.getLatitude())
                .willReturn(latitude);
        given(place.getLongitude())
                .willReturn(longitude);
        given(place.getArea())
                .willReturn(area);

        return place;
    }
}
