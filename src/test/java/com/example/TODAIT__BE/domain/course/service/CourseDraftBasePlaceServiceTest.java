package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.BasePlaceSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.BasePlaceSaveRequest.ExternalPlace;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.BasePlaceSaveResponse;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.code.CourseDraftErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftPlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.course.service.validator.CourseDraftValidator;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.place.entity.PlaceDataSource;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.entity.PlaceSource;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceReviewStatus;
import com.example.TODAIT__BE.domain.place.exception.PlaceException;
import com.example.TODAIT__BE.domain.place.code.ExternalPlaceRegistrationErrorCode;
import com.example.TODAIT__BE.domain.place.code.PlaceDetailErrorCode;
import com.example.TODAIT__BE.domain.place.repository.PlaceDataSourceRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceSourceRepository;
import com.example.TODAIT__BE.domain.place.service.ExternalPlaceRegistrationService;
import com.example.TODAIT__BE.domain.taxonomy.code.AreaErrorCode;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import com.example.TODAIT__BE.domain.taxonomy.exception.TaxonomyException;
import com.example.TODAIT__BE.domain.taxonomy.repository.AreaRepository;
import com.example.TODAIT__BE.domain.taxonomy.repository.PlaceCategoryRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseDraftBasePlaceServiceTest {

    @Mock
    private CourseDraftRepository courseDraftRepository;
    @Mock
    private CourseDraftPlaceRepository courseDraftPlaceRepository;
    @Mock
    private PlaceRepository placeRepository;
    @Mock
    private PlaceSourceRepository placeSourceRepository;
    @Mock
    private PlaceDataSourceRepository dataSourceRepository;
    @Mock
    private AreaRepository areaRepository;
    @Mock
    private PlaceCategoryRepository placeCategoryRepository;
    @Mock
    private ExternalPlaceRegistrationService externalPlaceRegistrationService;

    private CourseDraftService courseDraftService;

    @BeforeEach
    void setUp() {
        courseDraftService = new CourseDraftService(
                courseDraftRepository,
                null,
                null,
                null,
                courseDraftPlaceRepository,
                null,
                null,
                placeRepository,
                placeSourceRepository,
                dataSourceRepository,
                areaRepository,
                placeCategoryRepository,
                externalPlaceRegistrationService,
                new CourseDraftValidator(),
                new com.example.TODAIT__BE.domain.course.config.CourseDraftProperties(30, "0 0 3 * * *", 500, 20, true),
                Clock.systemDefaultZone()
        );
    }

    @Test
    void savesInternalPlaceAsBaseAndMovesDraftToPlaceSelecting() {
        CourseDraft draft = draft(CourseDraftStatus.BASE_PLACE_SELECTING);
        Place place = availablePlace(21L);

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(placeRepository.findById(21L)).willReturn(Optional.of(place));
        given(placeSourceRepository.findByPlaceIdAndIsPrimaryTrue(21L)).willReturn(Optional.empty());
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of());
        given(courseDraftPlaceRepository.save(any(CourseDraftPlace.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        BasePlaceSaveResponse response = courseDraftService.saveBasePlace(
                10L, 1L, new BasePlaceSaveRequest(21L, null)
        );

        assertThat(response.draftStatus()).isEqualTo(CourseDraftStatus.PLACE_SELECTING);
        assertThat(response.basePlace().placeId()).isEqualTo(21L);
        assertThat(response.basePlace().isNewPlace()).isFalse();
        assertThat(response.basePlace().sourceType()).isEqualTo("OPERATOR");
        assertThat(response.basePlace().visitOrder()).isEqualTo(1);
        assertThat(response.basePlace().placeRole()).isEqualTo(PlaceRole.BASE);
        assertThat(draft.getStatus()).isEqualTo(CourseDraftStatus.PLACE_SELECTING);
    }

    @Test
    void updatesExistingBaseDraftPlaceInsteadOfCreatingANewOne() {
        CourseDraft draft = draft(CourseDraftStatus.BASE_PLACE_SELECTING);
        Place oldPlace = availablePlace(21L);
        Place newPlace = availablePlace(22L);
        CourseDraftPlace existingBase = CourseDraftPlace.builder()
                .id(100L)
                .courseDraft(draft)
                .place(oldPlace)
                .visitOrder(1)
                .placeRole(PlaceRole.BASE)
                .build();

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(placeRepository.findById(22L)).willReturn(Optional.of(newPlace));
        given(placeSourceRepository.findByPlaceIdAndIsPrimaryTrue(22L)).willReturn(Optional.empty());
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of(existingBase));

        BasePlaceSaveResponse response = courseDraftService.saveBasePlace(
                10L, 1L, new BasePlaceSaveRequest(22L, null)
        );

        verify(courseDraftPlaceRepository, never()).save(any());
        assertThat(existingBase.getPlace()).isEqualTo(newPlace);
        assertThat(response.basePlace().placeId()).isEqualTo(22L);
    }

    @Test
    void swapsBaseWithExistingSelectedPlaceWhenSelectedPlaceBecomesBase() {
        CourseDraft draft = draft(CourseDraftStatus.BASE_PLACE_SELECTING);
        Place oldBasePlace = availablePlace(21L);
        Place newBasePlace = availablePlace(22L);
        CourseDraftPlace existingBase = CourseDraftPlace.builder()
                .id(100L)
                .courseDraft(draft)
                .place(oldBasePlace)
                .visitOrder(1)
                .placeRole(PlaceRole.BASE)
                .build();
        CourseDraftPlace existingSelected = CourseDraftPlace.builder()
                .id(101L)
                .courseDraft(draft)
                .place(newBasePlace)
                .visitOrder(2)
                .placeRole(PlaceRole.SELECTED)
                .build();

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(placeRepository.findById(22L)).willReturn(Optional.of(newBasePlace));
        given(placeSourceRepository.findByPlaceIdAndIsPrimaryTrue(22L)).willReturn(Optional.empty());
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of(existingBase, existingSelected));

        BasePlaceSaveResponse response = courseDraftService.saveBasePlace(
                10L, 1L, new BasePlaceSaveRequest(22L, null)
        );

        verify(courseDraftPlaceRepository, times(2)).flush();
        assertThat(existingSelected.getPlaceRole()).isEqualTo(PlaceRole.BASE);
        assertThat(existingSelected.getVisitOrder()).isEqualTo(1);
        assertThat(existingBase.getPlaceRole()).isEqualTo(PlaceRole.SELECTED);
        assertThat(existingBase.getVisitOrder()).isEqualTo(2);
        assertThat(existingBase.getPlace()).isEqualTo(oldBasePlace);
        assertThat(response.basePlace().placeId()).isEqualTo(22L);
        assertThat(response.basePlace().placeRole()).isEqualTo(PlaceRole.BASE);
    }

    @Test
    void createsNewPlaceAndPlaceSourceWhenExternalPlaceIsNotRegistered() {
        CourseDraft draft = draft(CourseDraftStatus.BASE_PLACE_SELECTING);
        PlaceDataSource kakao = dataSource(1L, "KAKAO");
        Area area = area(2L, "YEONNAM", true);
        PlaceCategory category = placeCategory(1L, "CAFE", "카페", true);
        ExternalPlace externalPlace = new ExternalPlace(
                "KAKAO", "1234567890", "연남동 카페 투데잇", "서울 마포구 연남동 123-4", "서울 마포구 동교로 00길 12",
                37.561234, 126.923456, "YEONNAM", "CAFE", "디저트 카페", "02-1234-5678",
                "https://place.map.kakao.com/1234567890"
        );

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(dataSourceRepository.findByCodeAndIsActiveTrue("KAKAO")).willReturn(Optional.of(kakao));
        given(areaRepository.findByCode("YEONNAM")).willReturn(Optional.of(area));
        given(placeCategoryRepository.findByCode("CAFE")).willReturn(Optional.of(category));
        given(placeSourceRepository.findByDataSourceAndSourcePlaceId(kakao, "1234567890"))
                .willReturn(Optional.empty());
        Place createdPlace = availablePlace(84L);
        given(externalPlaceRegistrationService.register(
                area, category, kakao,
                externalPlace.name(), externalPlace.address(), externalPlace.roadAddress(),
                externalPlace.latitude(), externalPlace.longitude(),
                externalPlace.phone(), externalPlace.subCategory(),
                externalPlace.sourcePlaceId(), externalPlace.sourceUrl()
        )).willReturn(createdPlace);
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of());
        given(courseDraftPlaceRepository.save(any(CourseDraftPlace.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        BasePlaceSaveResponse response = courseDraftService.saveBasePlace(
                10L, 1L, new BasePlaceSaveRequest(null, externalPlace)
        );

        verify(externalPlaceRegistrationService).register(
                area, category, kakao,
                externalPlace.name(), externalPlace.address(), externalPlace.roadAddress(),
                externalPlace.latitude(), externalPlace.longitude(),
                externalPlace.phone(), externalPlace.subCategory(),
                externalPlace.sourcePlaceId(), externalPlace.sourceUrl()
        );
        assertThat(response.basePlace().isNewPlace()).isTrue();
        assertThat(response.basePlace().placeId()).isEqualTo(84L);
        assertThat(response.basePlace().sourceType()).isEqualTo("KAKAO");
    }

    @Test
    void reusesExistingPlaceWhenUniqueConstraintRaceLosesToAnotherRequest() {
        CourseDraft draft = draft(CourseDraftStatus.BASE_PLACE_SELECTING);
        PlaceDataSource kakao = dataSource(1L, "KAKAO");
        Area area = area(2L, "YEONNAM", true);
        PlaceCategory category = placeCategory(1L, "CAFE", "카페", true);
        Place raceWinnerPlace = availablePlace(84L);
        PlaceSource raceWinnerSource = PlaceSource.builder()
                .place(raceWinnerPlace)
                .dataSource(kakao)
                .sourcePlaceId("1234567890")
                .isPrimary(true)
                .build();
        ExternalPlace externalPlace = new ExternalPlace(
                "KAKAO", "1234567890", "연남동 카페 투데잇", "서울 마포구 연남동 123-4", "서울 마포구 동교로 00길 12",
                37.561234, 126.923456, "YEONNAM", "CAFE", "디저트 카페", "02-1234-5678",
                "https://place.map.kakao.com/1234567890"
        );

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(dataSourceRepository.findByCodeAndIsActiveTrue("KAKAO")).willReturn(Optional.of(kakao));
        given(areaRepository.findByCode("YEONNAM")).willReturn(Optional.of(area));
        given(placeCategoryRepository.findByCode("CAFE")).willReturn(Optional.of(category));
        given(placeSourceRepository.findByDataSourceAndSourcePlaceId(kakao, "1234567890"))
                .willReturn(Optional.empty(), Optional.of(raceWinnerSource));
        given(externalPlaceRegistrationService.register(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).willThrow(new org.springframework.dao.DataIntegrityViolationException("unique violation"));
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of());
        given(courseDraftPlaceRepository.save(any(CourseDraftPlace.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        BasePlaceSaveResponse response = courseDraftService.saveBasePlace(
                10L, 1L, new BasePlaceSaveRequest(null, externalPlace)
        );

        assertThat(response.basePlace().isNewPlace()).isFalse();
        assertThat(response.basePlace().placeId()).isEqualTo(84L);
    }

    @Test
    void throwsWhenReusedExternalPlaceIsNotAvailable() {
        CourseDraft draft = draft(CourseDraftStatus.BASE_PLACE_SELECTING);
        PlaceDataSource kakao = dataSource(1L, "KAKAO");
        Area area = area(2L, "YEONNAM", true);
        PlaceCategory category = placeCategory(1L, "CAFE", "카페", true);
        Place unavailablePlace = Place.builder()
                .id(84L)
                .area(area)
                .placeCategory(category)
                .name("연남동 카페 투데잇")
                .address("서울 마포구 연남동 123-4")
                .latitude(37.561234)
                .longitude(126.923456)
                .exposureStatus(PlaceExposureStatus.INACTIVE)
                .reviewStatus(PlaceReviewStatus.APPROVED)
                .isActive(true)
                .build();
        PlaceSource existingSource = PlaceSource.builder()
                .place(unavailablePlace)
                .dataSource(kakao)
                .sourcePlaceId("1234567890")
                .isPrimary(true)
                .build();
        ExternalPlace externalPlace = new ExternalPlace(
                "KAKAO", "1234567890", "연남동 카페 투데잇", "서울 마포구 연남동 123-4", null,
                37.561234, 126.923456, "YEONNAM", "CAFE", null, null, null
        );

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(dataSourceRepository.findByCodeAndIsActiveTrue("KAKAO")).willReturn(Optional.of(kakao));
        given(areaRepository.findByCode("YEONNAM")).willReturn(Optional.of(area));
        given(placeCategoryRepository.findByCode("CAFE")).willReturn(Optional.of(category));
        given(placeSourceRepository.findByDataSourceAndSourcePlaceId(kakao, "1234567890"))
                .willReturn(Optional.of(existingSource));

        assertThatThrownBy(() -> courseDraftService.saveBasePlace(
                10L, 1L, new BasePlaceSaveRequest(null, externalPlace)
        ))
                .isInstanceOf(PlaceException.class)
                .extracting("errorCode")
                .isEqualTo(ExternalPlaceRegistrationErrorCode.PLACE_NOT_AVAILABLE);

        verify(externalPlaceRegistrationService, never()).register(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void reusesExistingPlaceWhenExternalPlaceIsAlreadyRegistered() {
        CourseDraft draft = draft(CourseDraftStatus.BASE_PLACE_SELECTING);
        PlaceDataSource kakao = dataSource(1L, "KAKAO");
        Area area = area(2L, "YEONNAM", true);
        PlaceCategory category = placeCategory(1L, "CAFE", "카페", true);
        Place existingPlace = availablePlace(84L);
        PlaceSource existingSource = PlaceSource.builder()
                .place(existingPlace)
                .dataSource(kakao)
                .sourcePlaceId("1234567890")
                .isPrimary(true)
                .build();
        ExternalPlace externalPlace = new ExternalPlace(
                "KAKAO", "1234567890", "연남동 카페 투데잇", "서울 마포구 연남동 123-4", null,
                37.561234, 126.923456, "YEONNAM", "CAFE", null, null, null
        );

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(dataSourceRepository.findByCodeAndIsActiveTrue("KAKAO")).willReturn(Optional.of(kakao));
        given(areaRepository.findByCode("YEONNAM")).willReturn(Optional.of(area));
        given(placeCategoryRepository.findByCode("CAFE")).willReturn(Optional.of(category));
        given(placeSourceRepository.findByDataSourceAndSourcePlaceId(kakao, "1234567890"))
                .willReturn(Optional.of(existingSource));
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of());
        given(courseDraftPlaceRepository.save(any(CourseDraftPlace.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        BasePlaceSaveResponse response = courseDraftService.saveBasePlace(
                10L, 1L, new BasePlaceSaveRequest(null, externalPlace)
        );

        verify(placeRepository, never()).saveAndFlush(any());
        verify(placeSourceRepository, never()).saveAndFlush(any());
        assertThat(response.basePlace().isNewPlace()).isFalse();
        assertThat(response.basePlace().placeId()).isEqualTo(84L);
    }

    @Test
    void throwsWhenBothPlaceIdAndExternalPlaceArePassed() {
        CourseDraft draft = draft(CourseDraftStatus.BASE_PLACE_SELECTING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        ExternalPlace externalPlace = new ExternalPlace(
                "KAKAO", "1", "n", "a", null, 0.0, 0.0, "YEONNAM", "CAFE", null, null, null
        );

        assertThatThrownBy(() -> courseDraftService.saveBasePlace(
                10L, 1L, new BasePlaceSaveRequest(21L, externalPlace)
        ))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.BASE_PLACE_SOURCE_CONFLICT);
    }

    @Test
    void throwsWhenNeitherPlaceIdNorExternalPlaceIsPassed() {
        CourseDraft draft = draft(CourseDraftStatus.BASE_PLACE_SELECTING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        assertThatThrownBy(() -> courseDraftService.saveBasePlace(
                10L, 1L, new BasePlaceSaveRequest(null, null)
        ))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.BASE_PLACE_SOURCE_MISSING);
    }

    @Test
    void throwsWhenDraftStatusIsNotBasePlaceSelecting() {
        CourseDraft draft = draft(CourseDraftStatus.PLACE_SELECTING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        assertThatThrownBy(() -> courseDraftService.saveBasePlace(
                10L, 1L, new BasePlaceSaveRequest(21L, null)
        ))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.BASE_PLACE_DRAFT_STATUS_CONFLICT);
    }

    @Test
    void throwsWhenMemberDoesNotOwnTheDraft() {
        CourseDraft draft = draft(CourseDraftStatus.BASE_PLACE_SELECTING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        assertThatThrownBy(() -> courseDraftService.saveBasePlace(
                10L, 999L, new BasePlaceSaveRequest(21L, null)
        ))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.COURSE_DRAFT_ACCESS_DENIED);
    }

    @Test
    void throwsWhenInternalPlaceIsNotFound() {
        CourseDraft draft = draft(CourseDraftStatus.BASE_PLACE_SELECTING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(placeRepository.findById(21L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> courseDraftService.saveBasePlace(
                10L, 1L, new BasePlaceSaveRequest(21L, null)
        ))
                .isInstanceOf(PlaceException.class)
                .extracting("errorCode")
                .isEqualTo(PlaceDetailErrorCode.PLACE_NOT_FOUND);
    }

    @Test
    void throwsWhenInternalPlaceIsNotAvailable() {
        CourseDraft draft = draft(CourseDraftStatus.BASE_PLACE_SELECTING);
        Place inactivePlace = Place.builder()
                .id(21L)
                .area(area(2L, "YEONNAM", true))
                .placeCategory(placeCategory(1L, "RESTAURANT", "식당", true))
                .name("애몽")
                .address("서울 마포구 연남로3길 13")
                .latitude(37.561234)
                .longitude(126.923456)
                .exposureStatus(PlaceExposureStatus.ACTIVE)
                .reviewStatus(PlaceReviewStatus.APPROVED)
                .isActive(false)
                .build();
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(placeRepository.findById(21L)).willReturn(Optional.of(inactivePlace));

        assertThatThrownBy(() -> courseDraftService.saveBasePlace(
                10L, 1L, new BasePlaceSaveRequest(21L, null)
        ))
                .isInstanceOf(PlaceException.class)
                .extracting("errorCode")
                .isEqualTo(ExternalPlaceRegistrationErrorCode.PLACE_NOT_AVAILABLE);
    }

    @Test
    void throwsWhenInternalPlaceIsSoftDeleted() {
        CourseDraft draft = draft(CourseDraftStatus.BASE_PLACE_SELECTING);
        Place deletedPlace = Place.builder()
                .id(21L)
                .area(area(2L, "YEONNAM", true))
                .placeCategory(placeCategory(1L, "RESTAURANT", "식당", true))
                .name("애몽")
                .address("서울 마포구 연남로3길 13")
                .latitude(37.561234)
                .longitude(126.923456)
                .exposureStatus(PlaceExposureStatus.ACTIVE)
                .reviewStatus(PlaceReviewStatus.APPROVED)
                .isActive(true)
                .deletedAt(LocalDateTime.now().minusDays(1))
                .build();
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(placeRepository.findById(21L)).willReturn(Optional.of(deletedPlace));

        assertThatThrownBy(() -> courseDraftService.saveBasePlace(
                10L, 1L, new BasePlaceSaveRequest(21L, null)
        ))
                .isInstanceOf(PlaceException.class)
                .extracting("errorCode")
                .isEqualTo(ExternalPlaceRegistrationErrorCode.PLACE_NOT_AVAILABLE);
    }

    @Test
    void throwsWhenExternalPlaceCoordinatesAreOutOfRange() {
        CourseDraft draft = draft(CourseDraftStatus.BASE_PLACE_SELECTING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        ExternalPlace externalPlace = new ExternalPlace(
                "KAKAO", "1", "n", "a", null, 91.0, 0.0, "YEONNAM", "CAFE", null, null, null
        );

        assertThatThrownBy(() -> courseDraftService.saveBasePlace(
                10L, 1L, new BasePlaceSaveRequest(null, externalPlace)
        ))
                .isInstanceOf(PlaceException.class)
                .extracting("errorCode")
                .isEqualTo(ExternalPlaceRegistrationErrorCode.INVALID_PLACE_COORDINATE);
    }

    @Test
    void throwsWhenAreaCodeIsNotSupported() {
        CourseDraft draft = draft(CourseDraftStatus.BASE_PLACE_SELECTING);
        PlaceDataSource kakao = dataSource(1L, "KAKAO");
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(dataSourceRepository.findByCodeAndIsActiveTrue("KAKAO")).willReturn(Optional.of(kakao));
        given(areaRepository.findByCode("UNKNOWN")).willReturn(Optional.empty());

        ExternalPlace externalPlace = new ExternalPlace(
                "KAKAO", "1", "n", "a", null, 0.0, 0.0, "UNKNOWN", "CAFE", null, null, null
        );

        assertThatThrownBy(() -> courseDraftService.saveBasePlace(
                10L, 1L, new BasePlaceSaveRequest(null, externalPlace)
        ))
                .isInstanceOf(TaxonomyException.class)
                .extracting("errorCode")
                .isEqualTo(AreaErrorCode.AREA_NOT_SUPPORTED);
    }

    private CourseDraft draft(CourseDraftStatus status) {
        return CourseDraft.builder()
                .id(10L)
                .member(Member.builder().id(1L).build())
                .status(status)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
    }

    private Place availablePlace(Long id) {
        return Place.builder()
                .id(id)
                .area(area(2L, "YEONNAM", true))
                .placeCategory(placeCategory(1L, "RESTAURANT", "식당", true))
                .name("애몽")
                .address("서울 마포구 연남로3길 13")
                .latitude(37.561234)
                .longitude(126.923456)
                .exposureStatus(PlaceExposureStatus.ACTIVE)
                .reviewStatus(PlaceReviewStatus.APPROVED)
                .isActive(true)
                .build();
    }

    private Area area(Long id, String code, boolean active) {
        Area area = mock(Area.class);
        lenient().when(area.getId()).thenReturn(id);
        lenient().when(area.getCode()).thenReturn(code);
        lenient().when(area.getName()).thenReturn(code);
        lenient().when(area.getIsActive()).thenReturn(active);
        return area;
    }

    private PlaceCategory placeCategory(Long id, String code, String name, boolean active) {
        PlaceCategory placeCategory = mock(PlaceCategory.class);
        lenient().when(placeCategory.getId()).thenReturn(id);
        lenient().when(placeCategory.getCode()).thenReturn(code);
        lenient().when(placeCategory.getName()).thenReturn(name);
        lenient().when(placeCategory.getIsActive()).thenReturn(active);
        return placeCategory;
    }

    private PlaceDataSource dataSource(Long id, String code) {
        PlaceDataSource dataSource = mock(PlaceDataSource.class);
        lenient().when(dataSource.getId()).thenReturn(id);
        lenient().when(dataSource.getCode()).thenReturn(code);
        return dataSource;
    }
}
