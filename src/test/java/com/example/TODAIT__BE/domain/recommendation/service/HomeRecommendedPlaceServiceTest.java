package com.example.TODAIT__BE.domain.recommendation.service;

import com.example.TODAIT__BE.domain.recommendation.dto.response.HomeRecommendationResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.member.code.MemberErrorCode;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.member.exception.MemberException;
import com.example.TODAIT__BE.domain.member.repository.MemberRepository;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceReviewStatus;
import com.example.TODAIT__BE.domain.place.repository.PlaceRepository;
import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationLog;
import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationResult;
import com.example.TODAIT__BE.domain.recommendation.exception.RecommendationException;
import com.example.TODAIT__BE.domain.recommendation.code.HomeRecommendationErrorCode;
import com.example.TODAIT__BE.domain.recommendation.code.RecommendationLogErrorCode;
import com.example.TODAIT__BE.domain.recommendation.code.RecommendedPlaceErrorCode;
import com.example.TODAIT__BE.domain.recommendation.repository.RecommendationLogRepository;
import com.example.TODAIT__BE.domain.recommendation.repository.RecommendationResultRepository;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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

        lenient().when(memberRepository.findById(MEMBER_ID))
                .thenReturn(Optional.of(mock(Member.class)));

        lenient().when(recommendationLogRepository.save(any(RecommendationLog.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
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

        HomeRecommendationResponse.PlaceList response =
                service.getHomeRecommendedPlaces(
                        MEMBER_ID,
                        null,
                        3,
                        USER_LATITUDE,
                        USER_LONGITUDE
                );

        assertThat(response.locationAvailable()).isTrue();

        assertThat(response.places())
                .extracting(HomeRecommendationResponse.PlaceItem::placeId)
                .containsExactly(3L, 2L, 1L);

        assertThat(response.places())
                .extracting(HomeRecommendationResponse.PlaceItem::isNearby)
                .containsExactly(true, true, false);

        assertThat(response.places())
                .extracting(HomeRecommendationResponse.PlaceItem::rank)
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

        HomeRecommendationResponse.PlaceList response =
                service.getHomeRecommendedPlaces(
                        MEMBER_ID,
                        null,
                        1,
                        USER_LATITUDE,
                        USER_LONGITUDE
                );

        HomeRecommendationResponse.PlaceItem place =
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

        HomeRecommendationResponse.PlaceList response =
                service.getHomeRecommendedPlaces(
                        MEMBER_ID,
                        null,
                        6,
                        null,
                        null
                );

        assertThat(response.locationAvailable()).isFalse();

        assertThat(response.places())
                .extracting(HomeRecommendationResponse.PlaceItem::placeId)
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
    void appliesCursorAfterFullRecommendationOrderIsCreated() {
        List<Place> candidates = List.of(
                place(1L, "HONGDAE", 1, 37.1, 127.1),
                place(2L, "HONGDAE", 2, 37.2, 127.2),
                place(3L, "SEONGSU", 1, 37.3, 127.3),
                place(4L, "SEONGSU", 2, 37.4, 127.4),
                place(5L, "YEONNAM", 1, 37.5, 127.5),
                place(6L, "YEONNAM", 2, 37.6, 127.6)
        );

        givenCandidates(candidates);

        HomeRecommendationResponse.PlaceList firstResponse =
                service.getHomeRecommendedPlaces(
                        MEMBER_ID,
                        null,
                        2,
                        null,
                        null
                );
        HomeRecommendationResponse.PlaceList secondResponse =
                service.getHomeRecommendedPlaces(
                        MEMBER_ID,
                        firstResponse.nextCursor(),
                        2,
                        null,
                        null
                );

        /*
         * 전체 순서:
         * 홍대1 → 성수1 → 연남1 → 홍대2 → 성수2 → 연남2
         *
         * 첫 응답의 nextCursor, size=2:
         * 연남1 → 홍대2
         */
        assertThat(firstResponse.hasNext()).isTrue();
        assertThat(firstResponse.nextCursor()).isNotBlank();
        assertThat(secondResponse.size()).isEqualTo(2);

        assertThat(secondResponse.places())
                .extracting(HomeRecommendationResponse.PlaceItem::placeId)
                .containsExactly(5L, 2L);

        assertThat(secondResponse.places())
                .extracting(HomeRecommendationResponse.PlaceItem::rank)
                .containsExactly(3, 4);
    }

    @Test
    void savesLogAndDoesNotSaveResultsWhenCandidatesAreEmpty() {
        givenCandidates(List.of());

        HomeRecommendationResponse.PlaceList response =
                service.getHomeRecommendedPlaces(
                        MEMBER_ID,
                        null,
                        null,
                        null,
                        null
                );

        assertThat(response.places()).isEmpty();
        assertThat(response.locationAvailable()).isFalse();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();

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
                        null,
                        2,
                        Double.NaN,
                        USER_LONGITUDE
                )
        )
                .isInstanceOfSatisfying(
                        RecommendationException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(
                                HomeRecommendationErrorCode
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
                        null,
                        2,
                        Double.POSITIVE_INFINITY,
                        USER_LONGITUDE
                )
        )
                .isInstanceOfSatisfying(
                        RecommendationException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(
                                HomeRecommendationErrorCode
                                        .INVALID_LOCATION_RANGE
                                )
                );

        assertThatThrownBy(() ->
                service.getHomeRecommendedPlaces(
                        MEMBER_ID,
                        null,
                        2,
                        USER_LATITUDE,
                        Double.NEGATIVE_INFINITY
                )
        )
                .isInstanceOfSatisfying(
                        RecommendationException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(
                                HomeRecommendationErrorCode
                                        .INVALID_LOCATION_RANGE
                                )
                );
    }

    @Test
    void throwsMemberNotFoundWhenMemberDoesNotExist() {
        given(memberRepository.findById(MEMBER_ID))
                .willReturn(Optional.empty());
        givenCandidates(List.of());

        assertThatThrownBy(() ->
                service.getHomeRecommendedPlaces(
                        MEMBER_ID,
                        null,
                        null,
                        null,
                        null
                )
        )
                .isInstanceOfSatisfying(
                        MemberException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND)
                );

        verify(recommendationLogRepository, never())
                .save(any(RecommendationLog.class));
    }

    @Test
    void throwsRecommendationExceptionWhenRequestContextSerializationFails()
            throws JsonProcessingException {
        ObjectMapper failingObjectMapper = mock(ObjectMapper.class);
        JsonProcessingException serializationFailure =
                new JsonProcessingException("boom") {
                };
        HomeRecommendedPlaceService failingService =
                new HomeRecommendedPlaceService(
                        placeRepository,
                        memberRepository,
                        recommendationLogRepository,
                        recommendationResultRepository,
                        failingObjectMapper
                );

        given(memberRepository.findById(MEMBER_ID))
                .willReturn(Optional.of(mock(Member.class)));
        givenCandidates(List.of());
        given(failingObjectMapper.writeValueAsString(any()))
                .willThrow(serializationFailure);

        assertThatThrownBy(() ->
                failingService.getHomeRecommendedPlaces(
                        MEMBER_ID,
                        null,
                        null,
                        null,
                        null
                )
        )
                .isInstanceOf(RecommendationException.class)
                .hasCause(serializationFailure)
                .extracting(exception ->
                        ((RecommendationException) exception).getErrorCode()
                )
                .isEqualTo(
                        RecommendationLogErrorCode.REQUEST_CONTEXT_SERIALIZATION_FAILED
                );

        verify(recommendationLogRepository, never())
                .save(any(RecommendationLog.class));
    }

    @Test
    void throwsInvalidCursorWhenCursorIsMalformed() {
        assertThatThrownBy(() ->
                service.getHomeRecommendedPlaces(
                        MEMBER_ID,
                        "invalid-cursor",
                        2,
                        null,
                        null
                )
        )
                .isInstanceOfSatisfying(
                        RecommendationException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(
                                        HomeRecommendationErrorCode.INVALID_CURSOR
                                )
                )
                .hasCauseInstanceOf(IllegalArgumentException.class);

        verify(placeRepository, never())
                .findHomeRecommendedPlaceCandidates(
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void throwsInvalidCursorWhenCursorSignatureIsTampered() {
        String tamperedCursor = unsignedCursor("2099-01-01:2:bad-signature");

        assertThatThrownBy(() ->
                service.getHomeRecommendedPlaces(
                        MEMBER_ID,
                        tamperedCursor,
                        2,
                        null,
                        null
                )
        )
                .isInstanceOfSatisfying(
                        RecommendationException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(
                                        HomeRecommendationErrorCode.INVALID_CURSOR
                                )
                )
                .hasCauseInstanceOf(IllegalArgumentException.class);

        verify(placeRepository, never())
                .findHomeRecommendedPlaceCandidates(
                        any(),
                        any(),
                        any()
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
        lenient().when(area.getId()).thenReturn(id * 10);
        lenient().when(area.getCode()).thenReturn(areaCode);
        lenient().when(area.getName()).thenReturn(areaName(areaCode));

        PlaceCategory category = mock(PlaceCategory.class);
        lenient().when(category.getId()).thenReturn(1L);
        lenient().when(category.getCode()).thenReturn("CAFE");
        lenient().when(category.getName()).thenReturn("카페");

        Place place = mock(Place.class);
        lenient().when(place.getId()).thenReturn(id);
        lenient().when(place.getName()).thenReturn("테스트 장소 " + id);
        lenient().when(place.getAddress()).thenReturn("테스트 주소 " + id);
        lenient().when(place.getRoadAddress()).thenReturn("테스트 도로명 주소 " + id);
        lenient().when(place.getLatitude()).thenReturn(latitude);
        lenient().when(place.getLongitude()).thenReturn(longitude);
        lenient().when(place.getArea()).thenReturn(area);
        lenient().when(place.getPlaceCategory()).thenReturn(category);
        lenient().when(place.getSubCategory()).thenReturn("감성 카페");
        lenient().when(place.getDefaultImageUrl()).thenReturn("https://image/" + id);
        lenient().when(place.getOperatorPriority()).thenReturn(operatorPriority);
        lenient().when(place.getCreatedAt())
                .thenReturn(LocalDateTime.of(2026, 7, 1, 0, 0));

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

    private String unsignedCursor(String raw) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
