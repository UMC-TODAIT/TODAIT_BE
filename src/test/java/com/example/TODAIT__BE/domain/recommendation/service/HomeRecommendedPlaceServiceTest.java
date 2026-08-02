package com.example.TODAIT__BE.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceReviewStatus;
import com.example.TODAIT__BE.domain.place.repository.PlaceRepository;
import com.example.TODAIT__BE.domain.recommendation.dto.response.HomeRecommendedPlaceListResponse;
import com.example.TODAIT__BE.domain.recommendation.dto.response.HomeRecommendedPlaceResponse;
import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationLog;
import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationResult;
import com.example.TODAIT__BE.domain.recommendation.exception.RecommendationException;
import com.example.TODAIT__BE.domain.recommendation.exception.code.RecommendationErrorCode;
import com.example.TODAIT__BE.domain.recommendation.repository.RecommendationLogRepository;
import com.example.TODAIT__BE.domain.recommendation.repository.RecommendationResultRepository;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeRecommendedPlaceServiceTest {

    private static final Long MEMBER_ID = 1L;

    private static final double USER_LATITUDE = 37.0;
    private static final double USER_LONGITUDE = 127.0;
    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private static final List<String> AREA_CODES =
            List.of("HONGDAE", "SEONGSU", "YEONNAM");

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RecommendationLogRepository recommendationLogRepository;

    @Mock
    private RecommendationResultRepository recommendationResultRepository;

    private HomeRecommendedPlaceService service;

    @BeforeEach
    void setUp() {
        service = new HomeRecommendedPlaceService(
                placeRepository,
                memberRepository,
                recommendationLogRepository,
                recommendationResultRepository,
                new ObjectMapper()
        );

        given(memberRepository.getReferenceById(MEMBER_ID))
                .willReturn(mock(Member.class));

        given(recommendationLogRepository.save(any(RecommendationLog.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void sortsNearbyPlacesFirstAndThenByDistance() {
        Place farPlace = place(
                1L,
                "HONGDAE",
                1,
                latitudeAtDistance(700),
                USER_LONGITUDE
        );

        Place nearPlace400 = place(
                2L,
                "HONGDAE",
                1,
                latitudeAtDistance(400),
                USER_LONGITUDE
        );

        Place nearPlace100 = place(
                3L,
                "HONGDAE",
                2,
                latitudeAtDistance(100),
                USER_LONGITUDE
        );

        givenCandidates(List.of(
                farPlace,
                nearPlace400,
                nearPlace100
        ));

        HomeRecommendedPlaceListResponse response =
                service.getHomeRecommendedPlaces(
                        MEMBER_ID,
                        0,
                        3,
                        USER_LATITUDE,
                        USER_LONGITUDE
                );

        assertThat(response.locationAvailable()).isTrue();

        assertThat(response.places())
                .extracting(HomeRecommendedPlaceResponse::placeId)
                .containsExactly(3L, 2L, 1L);

        assertThat(response.places())
                .extracting(HomeRecommendedPlaceResponse::isNearby)
                .containsExactly(true, true, false);

        assertThat(response.places())
                .extracting(HomeRecommendedPlaceResponse::rank)
                .containsExactly(1, 2, 3);

        ArgumentCaptor<List<RecommendationResult>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(recommendationResultRepository)
                .saveAll(captor.capture());

        assertThat(captor.getValue())
                .extracting(RecommendationResult::getRankNo)
                .containsExactly(1, 2, 3);

        assertThat(captor.getValue())
                .allSatisfy(result -> {
                    assertThat(result.getPlace()).isNotNull();
                    assertThat(result.getCourse()).isNull();
                });
    }

    @Test
    void treatsExactlyFiveHundredMetersAsNearby() {
        Place boundaryPlace = place(
                1L,
                "HONGDAE",
                1,
                latitudeAtDistance(500),
                USER_LONGITUDE
        );

        givenCandidates(List.of(boundaryPlace));

        HomeRecommendedPlaceListResponse response =
                service.getHomeRecommendedPlaces(
                        MEMBER_ID,
                        0,
                        1,
                        USER_LATITUDE,
                        USER_LONGITUDE
                );

        HomeRecommendedPlaceResponse place =
                response.places().get(0);

        assertThat(place.distanceMeters()).isEqualTo(500);
        assertThat(place.isNearby()).isTrue();
        assertThat(place.recommendationReason())
                .isEqualTo("현재 위치와 가까워요.");
    }

    @Test
    void appliesAreaRoundRobinWhenLocationIsUnavailable() {
        Place hongdae1 = place(1L, "HONGDAE", 1, 37.1, 127.1);
        Place hongdae2 = place(2L, "HONGDAE", 2, 37.2, 127.2);

        Place seongsu1 = place(3L, "SEONGSU", 1, 37.3, 127.3);
        Place seongsu2 = place(4L, "SEONGSU", 2, 37.4, 127.4);

        Place yeonnam1 = place(5L, "YEONNAM", 1, 37.5, 127.5);
        Place yeonnam2 = place(6L, "YEONNAM", 2, 37.6, 127.6);

        givenCandidates(List.of(
                hongdae2,
                seongsu2,
                yeonnam2,
                hongdae1,
                seongsu1,
                yeonnam1
        ));

        HomeRecommendedPlaceListResponse response =
                service.getHomeRecommendedPlaces(
                        MEMBER_ID,
                        0,
                        6,
                        null,
                        null
                );

        assertThat(response.locationAvailable()).isFalse();

        assertThat(response.places())
                .extracting(HomeRecommendedPlaceResponse::placeId)
                .containsExactly(
                        1L,
                        3L,
                        5L,
                        2L,
                        4L,
                        6L
                );

        assertThat(response.places())
                .allSatisfy(place -> {
                    assertThat(place.distanceMeters()).isNull();
                    assertThat(place.isNearby()).isNull();
                });
    }

    @Test
    void appliesPaginationAfterFullRecommendationOrderIsCreated() {
        List<Place> candidates = List.of(
                place(1L, "HONGDAE", 1, 37.1, 127.1),
                place(2L, "HONGDAE", 2, 37.2, 127.2),
                place(3L, "SEONGSU", 1, 37.3, 127.3),
                place(4L, "SEONGSU", 2, 37.4, 127.4),
                place(5L, "YEONNAM", 1, 37.5, 127.5),
                place(6L, "YEONNAM", 2, 37.6, 127.6)
        );

        givenCandidates(candidates);

        HomeRecommendedPlaceListResponse response =
                service.getHomeRecommendedPlaces(
                        MEMBER_ID,
                        1,
                        2,
                        null,
                        null
                );

        /*
         * 전체 순서:
         * 홍대1 → 성수1 → 연남1 → 홍대2 → 성수2 → 연남2
         *
         * page=1, size=2:
         * 연남1 → 홍대2
         */
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(2);

        assertThat(response.places())
                .extracting(HomeRecommendedPlaceResponse::placeId)
                .containsExactly(5L, 2L);

        /*
         * rank는 현재 응답 배열 기준으로 다시 1부터 부여된다.
         */
        assertThat(response.places())
                .extracting(HomeRecommendedPlaceResponse::rank)
                .containsExactly(1, 2);
    }

    @Test
    void savesLogAndDoesNotSaveResultsWhenCandidatesAreEmpty() {
        givenCandidates(List.of());

        HomeRecommendedPlaceListResponse response =
                service.getHomeRecommendedPlaces(
                        MEMBER_ID,
                        null,
                        null,
                        null,
                        null
                );

        assertThat(response.places()).isEmpty();
        assertThat(response.locationAvailable()).isFalse();

        verify(recommendationLogRepository)
                .save(any(RecommendationLog.class));

        verify(recommendationResultRepository, never())
                .saveAll(any());
    }

    @Test
    void rejectsNanCoordinates() {
        assertThatThrownBy(() ->
                service.getHomeRecommendedPlaces(
                        MEMBER_ID,
                        0,
                        2,
                        Double.NaN,
                        USER_LONGITUDE
                )
        )
                .isInstanceOfSatisfying(
                        RecommendationException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(
                                        RecommendationErrorCode
                                                .INVALID_LOCATION_RANGE
                                )
                );

        verify(placeRepository, never())
                .findHomeRecommendedPlaceCandidates(
                        any(),
                        any(),
                        any()
                );

        verify(recommendationLogRepository, never())
                .save(any());
    }

    @Test
    void rejectsInfiniteCoordinates() {
        assertThatThrownBy(() ->
                service.getHomeRecommendedPlaces(
                        MEMBER_ID,
                        0,
                        2,
                        Double.POSITIVE_INFINITY,
                        USER_LONGITUDE
                )
        )
                .isInstanceOfSatisfying(
                        RecommendationException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(
                                        RecommendationErrorCode
                                                .INVALID_LOCATION_RANGE
                                )
                );

        assertThatThrownBy(() ->
                service.getHomeRecommendedPlaces(
                        MEMBER_ID,
                        0,
                        2,
                        USER_LATITUDE,
                        Double.NEGATIVE_INFINITY
                )
        )
                .isInstanceOfSatisfying(
                        RecommendationException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(
                                        RecommendationErrorCode
                                                .INVALID_LOCATION_RANGE
                                )
                );
    }

    private void givenCandidates(List<Place> candidates) {
        given(
                placeRepository.findHomeRecommendedPlaceCandidates(
                        PlaceReviewStatus.APPROVED,
                        PlaceExposureStatus.ACTIVE,
                        AREA_CODES
                )
        ).willReturn(candidates);
    }

    private Place place(
            Long id,
            String areaCode,
            int operatorPriority,
            double latitude,
            double longitude
    ) {
        Area area = mock(Area.class);
        given(area.getId()).willReturn(id * 10);
        given(area.getCode()).willReturn(areaCode);
        given(area.getName()).willReturn(areaName(areaCode));

        PlaceCategory category = mock(PlaceCategory.class);
        given(category.getId()).willReturn(1L);
        given(category.getCode()).willReturn("CAFE");
        given(category.getName()).willReturn("카페");

        Place place = mock(Place.class);
        given(place.getId()).willReturn(id);
        given(place.getName()).willReturn("테스트 장소 " + id);
        given(place.getAddress()).willReturn("테스트 주소 " + id);
        given(place.getRoadAddress()).willReturn("테스트 도로명 주소 " + id);
        given(place.getLatitude()).willReturn(latitude);
        given(place.getLongitude()).willReturn(longitude);
        given(place.getArea()).willReturn(area);
        given(place.getPlaceCategory()).willReturn(category);
        given(place.getSubCategory()).willReturn("감성 카페");
        given(place.getDefaultImageUrl()).willReturn("https://image/" + id);
        given(place.getOperatorPriority()).willReturn(operatorPriority);
        given(place.getCreatedAt())
                .willReturn(LocalDateTime.of(2026, 7, 1, 0, 0));

        return place;
    }

    private String areaName(String areaCode) {
        return switch (areaCode) {
            case "HONGDAE" -> "홍대";
            case "SEONGSU" -> "성수";
            case "YEONNAM" -> "연남";
            default -> areaCode;
        };
    }

    private double latitudeAtDistance(double distanceMeters) {
        double latitudeDeltaRadians =
                distanceMeters / EARTH_RADIUS_METERS;

        return USER_LATITUDE
                + Math.toDegrees(latitudeDeltaRadians);
    }
}
