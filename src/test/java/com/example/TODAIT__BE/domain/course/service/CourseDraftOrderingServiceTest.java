package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.OrderingEntryResponse;
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
import com.example.TODAIT__BE.domain.place.entity.Place;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CourseDraftOrderingServiceTest {

    private static final Long DRAFT_ID = 15L;
    private static final Long MEMBER_ID = 1L;

    @Mock
    private CourseDraftRepository courseDraftRepository;
    @Mock
    private CourseDraftPlaceRepository courseDraftPlaceRepository;

    private CourseDraftService service;

    @BeforeEach
    void setUp() {
        service = new CourseDraftService(
                courseDraftRepository,
                null,
                null,
                null,
                courseDraftPlaceRepository,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                new CourseDraftValidator()
        );
    }

    @Test
    void transitionsToOrderingAndReturnsPlacesOnFirstEntry() {
        CourseDraft draft = draft(CourseDraftStatus.PLACE_SELECTING, MEMBER_ID);
        List<CourseDraftPlace> places = List.of(
                place(101L, 21L, 1, PlaceRole.BASE, "쥬노이"),
                place(102L, 35L, 2, PlaceRole.SELECTED, "코이르")
        );
        given(courseDraftRepository.findByIdForUpdate(DRAFT_ID))
                .willReturn(Optional.of(draft));
        given(courseDraftPlaceRepository
                .findByCourseDraftIdWithPlaceOrderByVisitOrderAsc(DRAFT_ID))
                .willReturn(places);

        OrderingEntryResponse response = service.enterOrdering(DRAFT_ID, MEMBER_ID);

        assertThat(draft.getStatus()).isEqualTo(CourseDraftStatus.ORDERING);
        assertThat(response.draftStatus()).isEqualTo(CourseDraftStatus.ORDERING);
        assertThat(response.totalPlaceCount()).isEqualTo(2);
        assertThat(response.selectedPlaceCount()).isEqualTo(1);
        assertThat(response.places()).hasSize(2);
        // BASE = 드래그/삭제 불가, SELECTED = 가능
        assertThat(response.places().get(0).placeRole()).isEqualTo(PlaceRole.BASE);
        assertThat(response.places().get(0).draggable()).isFalse();
        assertThat(response.places().get(0).deletable()).isFalse();
        assertThat(response.places().get(1).placeRole()).isEqualTo(PlaceRole.SELECTED);
        assertThat(response.places().get(1).draggable()).isTrue();
        assertThat(response.places().get(1).deletable()).isTrue();
    }

    @Test
    void keepsOrderingStatusOnReentry() {
        CourseDraft draft = draft(CourseDraftStatus.ORDERING, MEMBER_ID);
        // 중첩 given() 방지: place mock 리스트를 먼저 만든 뒤 스텁에 전달
        List<CourseDraftPlace> places = List.of(
                place(101L, 21L, 1, PlaceRole.BASE, "쥬노이"),
                place(102L, 35L, 2, PlaceRole.SELECTED, "코이르")
        );
        given(courseDraftRepository.findByIdForUpdate(DRAFT_ID))
                .willReturn(Optional.of(draft));
        given(courseDraftPlaceRepository
                .findByCourseDraftIdWithPlaceOrderByVisitOrderAsc(DRAFT_ID))
                .willReturn(places);

        OrderingEntryResponse response = service.enterOrdering(DRAFT_ID, MEMBER_ID);

        assertThat(draft.getStatus()).isEqualTo(CourseDraftStatus.ORDERING);
        assertThat(response.draftStatus()).isEqualTo(CourseDraftStatus.ORDERING);
        assertThat(response.places()).hasSize(2);
    }

    @Test
    void throwsConflictWhenStatusIsNotEnterable() {
        CourseDraft draft = draft(CourseDraftStatus.MOOD_SELECTING, MEMBER_ID);
        given(courseDraftRepository.findByIdForUpdate(DRAFT_ID))
                .willReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.enterOrdering(DRAFT_ID, MEMBER_ID))
                .isInstanceOfSatisfying(CourseException.class, e ->
                        assertThat(e.getErrorCode())
                                .isEqualTo(CourseDraftErrorCode.ORDERING_ENTRY_STATUS_CONFLICT));

        verify(courseDraftPlaceRepository, never())
                .findByCourseDraftIdWithPlaceOrderByVisitOrderAsc(any());
    }

    @Test
    void throwsNotFoundWhenDraftMissing() {
        given(courseDraftRepository.findByIdForUpdate(DRAFT_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.enterOrdering(DRAFT_ID, MEMBER_ID))
                .isInstanceOfSatisfying(CourseException.class, e ->
                        assertThat(e.getErrorCode())
                                .isEqualTo(CourseDraftErrorCode.COURSE_DRAFT_NOT_FOUND));
    }

    @Test
    void throwsAccessDeniedWhenNotOwner() {
        CourseDraft draft = draft(CourseDraftStatus.PLACE_SELECTING, 999L);
        given(courseDraftRepository.findByIdForUpdate(DRAFT_ID))
                .willReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.enterOrdering(DRAFT_ID, MEMBER_ID))
                .isInstanceOfSatisfying(CourseException.class, e ->
                        assertThat(e.getErrorCode())
                                .isEqualTo(CourseDraftErrorCode.COURSE_DRAFT_ACCESS_DENIED));
    }

    @Test
    void throwsBasePlaceConflictWhenNoBase() {
        givenPlaces(CourseDraftStatus.PLACE_SELECTING,
                place(102L, 35L, 1, PlaceRole.SELECTED, "코이르"));

        assertConflict(CourseDraftErrorCode.ORDERING_ENTRY_INVALID_BASE_PLACE);
    }

    @Test
    void throwsBasePlaceConflictWhenBaseVisitOrderIsNotFirst() {
        givenPlaces(CourseDraftStatus.PLACE_SELECTING,
                place(102L, 35L, 1, PlaceRole.SELECTED, "코이르"),
                place(101L, 21L, 2, PlaceRole.BASE, "쥬노이"));

        assertConflict(CourseDraftErrorCode.ORDERING_ENTRY_INVALID_BASE_PLACE);
    }

    @Test
    void throwsSelectedRequiredWhenOnlyBaseExists() {
        givenPlaces(CourseDraftStatus.PLACE_SELECTING,
                place(101L, 21L, 1, PlaceRole.BASE, "쥬노이"));

        assertConflict(CourseDraftErrorCode.ORDERING_ENTRY_SELECTED_PLACE_REQUIRED);
    }

    @Test
    void throwsVisitOrderConflictWhenNotContiguous() {
        givenPlaces(CourseDraftStatus.PLACE_SELECTING,
                place(101L, 21L, 1, PlaceRole.BASE, "쥬노이"),
                place(102L, 35L, 3, PlaceRole.SELECTED, "코이르"));

        assertConflict(CourseDraftErrorCode.ORDERING_ENTRY_INVALID_VISIT_ORDER);
    }

    private void givenPlaces(CourseDraftStatus status, CourseDraftPlace... places) {
        CourseDraft draft = draft(status, MEMBER_ID);
        given(courseDraftRepository.findByIdForUpdate(DRAFT_ID))
                .willReturn(Optional.of(draft));
        given(courseDraftPlaceRepository
                .findByCourseDraftIdWithPlaceOrderByVisitOrderAsc(DRAFT_ID))
                .willReturn(List.of(places));
    }

    private void assertConflict(CourseDraftErrorCode expected) {
        assertThatThrownBy(() -> service.enterOrdering(DRAFT_ID, MEMBER_ID))
                .isInstanceOfSatisfying(CourseException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(expected));
    }

    private CourseDraft draft(CourseDraftStatus status, Long ownerId) {
        Member member = mock(Member.class);
        given(member.getId()).willReturn(ownerId);
        return CourseDraft.builder()
                .id(DRAFT_ID)
                .member(member)
                .status(status)
                .build();
    }

    private CourseDraftPlace place(
            Long id, Long placeId, int visitOrder, PlaceRole role, String name) {
        Place place = mock(Place.class);
        given(place.getId()).willReturn(placeId);
        given(place.getName()).willReturn(name);
        given(place.getAddress()).willReturn("서울 마포구 연남동");
        given(place.getRoadAddress()).willReturn("서울 마포구 연희로");
        given(place.getLatitude()).willReturn(37.56);
        given(place.getLongitude()).willReturn(126.92);
        return CourseDraftPlace.builder()
                .id(id)
                .place(place)
                .visitOrder(visitOrder)
                .placeRole(role)
                .build();
    }
}
