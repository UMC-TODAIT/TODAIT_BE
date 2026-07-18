package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.example.TODAIT__BE.domain.course.dto.request.PlaceOrderUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.request.PlaceOrderUpdateRequest.PlaceOrderItem;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.exception.code.CourseErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftPlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.place.entity.Place;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseDraftPlaceServiceTest {

    @Mock
    private CourseDraftRepository courseDraftRepository;
    @Mock
    private CourseDraftPlaceRepository courseDraftPlaceRepository;

    private CourseDraftPlaceService courseDraftPlaceService;

    @BeforeEach
    void setUp() {
        courseDraftPlaceService = new CourseDraftPlaceService(courseDraftRepository, courseDraftPlaceRepository);
    }

    private Member member(Long id) {
        return Member.builder().id(id).build();
    }

    private CourseDraft courseDraft(Long id, Member owner) {
        return CourseDraft.builder().id(id).member(owner).build();
    }

    private CourseDraftPlace draftPlace(Long id, PlaceRole role, int visitOrder) {
        return CourseDraftPlace.builder()
                .id(id)
                .placeRole(role)
                .visitOrder(visitOrder)
                .place(mock(Place.class))
                .build();
    }

    @Test
    void throwsWhenRequesterIsNotOwner() {
        CourseDraft draft = courseDraft(10L, member(1L));
        given(courseDraftRepository.findById(10L)).willReturn(Optional.of(draft));

        PlaceOrderUpdateRequest request = new PlaceOrderUpdateRequest(List.of(new PlaceOrderItem(100L, 2)));

        assertThatThrownBy(() -> courseDraftPlaceService.updatePlaceOrder(10L, 2L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.NOT_COURSE_DRAFT_OWNER);
    }

    @Test
    void throwsWhenPlaceIdDoesNotBelongToDraft() {
        CourseDraft draft = courseDraft(10L, member(1L));
        given(courseDraftRepository.findById(10L)).willReturn(Optional.of(draft));

        CourseDraftPlace base = draftPlace(100L, PlaceRole.BASE, 1);
        CourseDraftPlace selected = draftPlace(101L, PlaceRole.SELECTED, 2);
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of(base, selected));

        PlaceOrderUpdateRequest request = new PlaceOrderUpdateRequest(List.of(new PlaceOrderItem(999L, 2)));

        assertThatThrownBy(() -> courseDraftPlaceService.updatePlaceOrder(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.SELECTED_PLACE_NOT_FOUND);
    }

    @Test
    void throwsWhenBasePlaceIncludedInRequest() {
        CourseDraft draft = courseDraft(10L, member(1L));
        given(courseDraftRepository.findById(10L)).willReturn(Optional.of(draft));

        CourseDraftPlace base = draftPlace(100L, PlaceRole.BASE, 1);
        CourseDraftPlace selected = draftPlace(101L, PlaceRole.SELECTED, 2);
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of(base, selected));

        PlaceOrderUpdateRequest request = new PlaceOrderUpdateRequest(List.of(new PlaceOrderItem(100L, 2)));

        assertThatThrownBy(() -> courseDraftPlaceService.updatePlaceOrder(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.BASE_PLACE_NOT_REORDERABLE);
    }

    @Test
    void throwsWhenVisitOrderNotContinuousFromTwo() {
        CourseDraft draft = courseDraft(10L, member(1L));
        given(courseDraftRepository.findById(10L)).willReturn(Optional.of(draft));

        CourseDraftPlace base = draftPlace(100L, PlaceRole.BASE, 1);
        CourseDraftPlace s1 = draftPlace(101L, PlaceRole.SELECTED, 2);
        CourseDraftPlace s2 = draftPlace(102L, PlaceRole.SELECTED, 3);
        CourseDraftPlace s3 = draftPlace(103L, PlaceRole.SELECTED, 4);
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of(base, s1, s2, s3));

        // 2, 4, 5 : gap at 3
        PlaceOrderUpdateRequest request = new PlaceOrderUpdateRequest(List.of(
                new PlaceOrderItem(101L, 2),
                new PlaceOrderItem(102L, 4),
                new PlaceOrderItem(103L, 5)
        ));

        assertThatThrownBy(() -> courseDraftPlaceService.updatePlaceOrder(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.INVALID_VISIT_ORDER);
    }

    @Test
    void throwsWhenVisitOrderHasDuplicate() {
        CourseDraft draft = courseDraft(10L, member(1L));
        given(courseDraftRepository.findById(10L)).willReturn(Optional.of(draft));

        CourseDraftPlace base = draftPlace(100L, PlaceRole.BASE, 1);
        CourseDraftPlace s1 = draftPlace(101L, PlaceRole.SELECTED, 2);
        CourseDraftPlace s2 = draftPlace(102L, PlaceRole.SELECTED, 3);
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of(base, s1, s2));

        // 2, 2 : duplicate
        PlaceOrderUpdateRequest request = new PlaceOrderUpdateRequest(List.of(
                new PlaceOrderItem(101L, 2),
                new PlaceOrderItem(102L, 2)
        ));

        assertThatThrownBy(() -> courseDraftPlaceService.updatePlaceOrder(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.INVALID_VISIT_ORDER);
    }

    @Test
    void throwsWhenSelectedPlaceIsMissingFromRequest() {
        CourseDraft draft = courseDraft(10L, member(1L));
        given(courseDraftRepository.findById(10L)).willReturn(Optional.of(draft));

        CourseDraftPlace base = draftPlace(100L, PlaceRole.BASE, 1);
        CourseDraftPlace s1 = draftPlace(101L, PlaceRole.SELECTED, 2);
        CourseDraftPlace s2 = draftPlace(102L, PlaceRole.SELECTED, 3);
        CourseDraftPlace s3 = draftPlace(103L, PlaceRole.SELECTED, 4);
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of(base, s1, s2, s3));

        // only 2 of 3 selected places submitted
        PlaceOrderUpdateRequest request = new PlaceOrderUpdateRequest(List.of(
                new PlaceOrderItem(101L, 2),
                new PlaceOrderItem(102L, 3)
        ));

        assertThatThrownBy(() -> courseDraftPlaceService.updatePlaceOrder(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseErrorCode.INVALID_VISIT_ORDER);
    }
}
