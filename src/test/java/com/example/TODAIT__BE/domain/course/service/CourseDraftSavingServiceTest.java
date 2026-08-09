package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.SavingEnterResponse;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseDraftSavingServiceTest {

    @Mock
    private CourseDraftRepository courseDraftRepository;
    @Mock
    private CourseDraftPlaceRepository courseDraftPlaceRepository;

    private CourseDraftSavingService courseDraftSavingService;

    @BeforeEach
    void setUp() {
        courseDraftSavingService = new CourseDraftSavingService(
                courseDraftRepository,
                courseDraftPlaceRepository,
                new CourseDraftValidator()
        );
    }

    @Test
    void enterSavingChangesOrderingDraftToSavingAndReturnsRoutePreview() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.ORDERING);
        CourseDraftPlace base = draftPlace(100L, PlaceRole.BASE, 1, place(1000L, "base"));
        CourseDraftPlace selected = draftPlace(101L, PlaceRole.SELECTED, 2, place(1001L, "selected"));

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(courseDraftPlaceRepository.findByCourseDraftWithPlaceOrderByVisitOrderAsc(draft))
                .willReturn(List.of(base, selected));

        SavingEnterResponse response = courseDraftSavingService.enterSaving(10L, 1L);

        assertThat(draft.getStatus()).isEqualTo(CourseDraftStatus.SAVING);
        assertThat(response.courseDraftId()).isEqualTo(10L);
        assertThat(response.draftStatus()).isEqualTo(CourseDraftStatus.SAVING);
        assertThat(response.totalPlaceCount()).isEqualTo(2);
        assertThat(response.routePreview()).hasSize(2);
        assertThat(response.routePreview().get(0).placeRole()).isEqualTo(PlaceRole.BASE);
        assertThat(response.routePreview().get(1).visitOrder()).isEqualTo(2);
    }

    @Test
    void enterSavingReturnsRoutePreviewWhenDraftIsAlreadySaving() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.SAVING);
        CourseDraftPlace base = draftPlace(100L, PlaceRole.BASE, 1, place(1000L, "base"));
        CourseDraftPlace selected = draftPlace(101L, PlaceRole.SELECTED, 2, place(1001L, "selected"));

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(courseDraftPlaceRepository.findByCourseDraftWithPlaceOrderByVisitOrderAsc(draft))
                .willReturn(List.of(base, selected));

        SavingEnterResponse response = courseDraftSavingService.enterSaving(10L, 1L);

        assertThat(draft.getStatus()).isEqualTo(CourseDraftStatus.SAVING);
        assertThat(response.courseDraftId()).isEqualTo(10L);
        assertThat(response.draftStatus()).isEqualTo(CourseDraftStatus.SAVING);
        assertThat(response.totalPlaceCount()).isEqualTo(2);
        assertThat(response.routePreview()).hasSize(2);
        assertThat(response.routePreview().get(0).placeRole()).isEqualTo(PlaceRole.BASE);
        assertThat(response.routePreview().get(1).visitOrder()).isEqualTo(2);
    }

    @Test
    void throwsWhenRequesterIsNotOwner() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.ORDERING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        assertThatThrownBy(() -> courseDraftSavingService.enterSaving(10L, 2L))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.COURSE_DRAFT_ACCESS_DENIED);
    }

    @ParameterizedTest
    @EnumSource(
            value = CourseDraftStatus.class,
            names = {"ORDERING", "SAVING"},
            mode = EnumSource.Mode.EXCLUDE
    )
    void throwsConflictWhenDraftStatusCannotEnterSaving(CourseDraftStatus status) {
        CourseDraft draft = courseDraft(10L, member(1L), status);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        assertThatThrownBy(() -> courseDraftSavingService.enterSaving(10L, 1L))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.COURSE_DRAFT_STATUS_CONFLICT);
    }

    @Test
    void throwsConflictWhenBasePlaceIsMissing() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.ORDERING);
        CourseDraftPlace selected = draftPlace(101L, PlaceRole.SELECTED, 2, mock(Place.class));

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(courseDraftPlaceRepository.findByCourseDraftWithPlaceOrderByVisitOrderAsc(draft))
                .willReturn(List.of(selected));

        assertThatThrownBy(() -> courseDraftSavingService.enterSaving(10L, 1L))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.COURSE_DRAFT_BASE_PLACE_CONFLICT);
    }

    @Test
    void throwsConflictWhenSelectedPlaceIsMissing() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.ORDERING);
        CourseDraftPlace base = draftPlace(100L, PlaceRole.BASE, 1, mock(Place.class));

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(courseDraftPlaceRepository.findByCourseDraftWithPlaceOrderByVisitOrderAsc(draft))
                .willReturn(List.of(base));

        assertThatThrownBy(() -> courseDraftSavingService.enterSaving(10L, 1L))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.COURSE_DRAFT_SELECTED_PLACE_CONFLICT);
    }

    @Test
    void throwsConflictWhenSelectedVisitOrderIsNotContinuousFromTwo() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.ORDERING);
        CourseDraftPlace base = draftPlace(100L, PlaceRole.BASE, 1, mock(Place.class));
        CourseDraftPlace selected = draftPlace(101L, PlaceRole.SELECTED, 3, mock(Place.class));

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(courseDraftPlaceRepository.findByCourseDraftWithPlaceOrderByVisitOrderAsc(draft))
                .willReturn(List.of(base, selected));

        assertThatThrownBy(() -> courseDraftSavingService.enterSaving(10L, 1L))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.COURSE_DRAFT_SELECTED_PLACE_CONFLICT);
    }

    private Member member(Long id) {
        return Member.builder().id(id).build();
    }

    private CourseDraft courseDraft(Long id, Member owner, CourseDraftStatus status) {
        return CourseDraft.builder()
                .id(id)
                .member(owner)
                .status(status)
                .build();
    }

    private CourseDraftPlace draftPlace(Long id, PlaceRole role, int visitOrder, Place place) {
        return CourseDraftPlace.builder()
                .id(id)
                .placeRole(role)
                .visitOrder(visitOrder)
                .place(place)
                .build();
    }

    private Place place(Long id, String name) {
        Place place = mock(Place.class);
        given(place.getId()).willReturn(id);
        given(place.getName()).willReturn(name);
        given(place.getAddress()).willReturn(name + " address");
        given(place.getLatitude()).willReturn(37.0);
        given(place.getLongitude()).willReturn(127.0);
        return place;
    }
}
