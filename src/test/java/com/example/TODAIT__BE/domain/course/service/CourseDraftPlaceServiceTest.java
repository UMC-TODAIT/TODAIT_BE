package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.PlaceAddRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.PlaceOrderUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.PlaceOrderUpdateRequest.PlaceOrderItem;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.PlaceAddResponse;
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
import com.example.TODAIT__BE.domain.place.code.ExternalPlaceRegistrationErrorCode;
import com.example.TODAIT__BE.domain.place.code.PlaceDetailErrorCode;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceReviewStatus;
import com.example.TODAIT__BE.domain.place.exception.PlaceException;
import com.example.TODAIT__BE.domain.place.repository.PlaceRepository;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
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
    @Mock
    private PlaceRepository placeRepository;

    private CourseDraftService courseDraftService;

    @BeforeEach
    void setUp() {
        courseDraftService =
                new CourseDraftService(
                        courseDraftRepository,
                        null,
                        null,
                        null,
                        courseDraftPlaceRepository,
                        null,
                        null,
                        placeRepository,
                        null,
                        null,
                        null,
                        null,
                        null,
                        new CourseDraftValidator()
                );
    }

    private Member member(Long id) {
        return Member.builder().id(id).build();
    }

    private CourseDraft courseDraft(Long id, Member owner) {
        return CourseDraft.builder()
                .id(id)
                .member(owner)
                .status(CourseDraftStatus.ORDERING)
                .build();
    }

    private CourseDraft courseDraft(Long id, Member owner, CourseDraftStatus status) {
        return CourseDraft.builder()
                .id(id)
                .member(owner)
                .status(status)
                .build();
    }

    private CourseDraftPlace draftPlace(Long id, PlaceRole role, int visitOrder) {
        return CourseDraftPlace.builder()
                .id(id)
                .placeRole(role)
                .visitOrder(visitOrder)
                .place(mock(Place.class))
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

    private PlaceCategory placeCategory(Long id, boolean active) {
        return placeCategory(id, active, "CAFE");
    }

    private PlaceCategory placeCategory(Long id, boolean active, String code) {
        PlaceCategory placeCategory = mock(PlaceCategory.class);
        lenient().when(placeCategory.getId()).thenReturn(id);
        lenient().when(placeCategory.getIsActive()).thenReturn(active);
        lenient().when(placeCategory.getCode()).thenReturn(code);
        return placeCategory;
    }

    private Area area(Long id, boolean active) {
        Area area = mock(Area.class);
        lenient().when(area.getId()).thenReturn(id);
        lenient().when(area.getIsActive()).thenReturn(active);
        return area;
    }

    private Place availablePlace(Long id, PlaceCategory category) {
        return Place.builder()
                .id(id)
                .area(area(1L, true))
                .placeCategory(category)
                .name("장소" + id)
                .address("서울 마포구")
                .latitude(37.5)
                .longitude(126.9)
                .exposureStatus(PlaceExposureStatus.ACTIVE)
                .reviewStatus(PlaceReviewStatus.APPROVED)
                .isActive(true)
                .build();
    }

    @Test
    void throwsWhenRequesterIsNotOwner() {
        CourseDraft draft = courseDraft(10L, member(1L));
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        PlaceOrderUpdateRequest request = new PlaceOrderUpdateRequest(List.of(new PlaceOrderItem(100L, 2)));

        assertThatThrownBy(() -> courseDraftService.updatePlaceOrder(10L, 2L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.COURSE_DRAFT_ACCESS_DENIED);
    }

    @Test
    void throwsWhenPlaceIdDoesNotBelongToDraft() {
        CourseDraft draft = courseDraft(10L, member(1L));
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        CourseDraftPlace base = draftPlace(100L, PlaceRole.BASE, 1);
        CourseDraftPlace selected = draftPlace(101L, PlaceRole.SELECTED, 2);
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of(base, selected));

        PlaceOrderUpdateRequest request = new PlaceOrderUpdateRequest(List.of(new PlaceOrderItem(999L, 2)));

        assertThatThrownBy(() -> courseDraftService.updatePlaceOrder(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.SELECTED_PLACE_NOT_FOUND);
    }

    @Test
    void reassignsBaseToFirstRequestedPlace() {
        CourseDraft draft = courseDraft(10L, member(1L));
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        CourseDraftPlace base = draftPlace(100L, PlaceRole.BASE, 1);
        CourseDraftPlace selected = draftPlace(101L, PlaceRole.SELECTED, 2);
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of(base, selected));

        PlaceOrderUpdateRequest request = new PlaceOrderUpdateRequest(List.of(
                new PlaceOrderItem(101L, 1),
                new PlaceOrderItem(100L, 2)
        ));

        courseDraftService.updatePlaceOrder(10L, 1L, request);

        assertThat(selected.getPlaceRole()).isEqualTo(PlaceRole.BASE);
        assertThat(selected.getVisitOrder()).isEqualTo(1);
        assertThat(base.getPlaceRole()).isEqualTo(PlaceRole.SELECTED);
        assertThat(base.getVisitOrder()).isEqualTo(2);
        verify(courseDraftRepository).touchUpdatedAt(10L);
    }

    @Test
    void throwsWhenDraftIsCompleted() {
        CourseDraft draft = CourseDraft.builder()
                .id(10L)
                .member(member(1L))
                .status(CourseDraftStatus.COMPLETED)
                .build();
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        PlaceOrderUpdateRequest request = new PlaceOrderUpdateRequest(List.of(new PlaceOrderItem(101L, 2)));

        assertThatThrownBy(() -> courseDraftService.updatePlaceOrder(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.PLACE_ORDER_DRAFT_STATUS_CONFLICT);
    }

    @Test
    void throwsWhenDraftStatusIsNotOrdering() {
        PlaceOrderUpdateRequest request = new PlaceOrderUpdateRequest(List.of(new PlaceOrderItem(101L, 2)));

        for (CourseDraftStatus status : CourseDraftStatus.values()) {
            if (status == CourseDraftStatus.ORDERING) {
                continue;
            }
            CourseDraft draft = CourseDraft.builder()
                    .id(10L)
                    .member(member(1L))
                    .status(status)
                    .build();
            given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

            assertThatThrownBy(() -> courseDraftService.updatePlaceOrder(10L, 1L, request))
                    .isInstanceOf(CourseException.class)
                    .extracting("errorCode")
                    .isEqualTo(CourseDraftErrorCode.PLACE_ORDER_DRAFT_STATUS_CONFLICT);
        }
    }

    @Test
    void throwsWhenVisitOrderNotContinuousFromOne() {
        CourseDraft draft = courseDraft(10L, member(1L));
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        CourseDraftPlace base = draftPlace(100L, PlaceRole.BASE, 1);
        CourseDraftPlace s1 = draftPlace(101L, PlaceRole.SELECTED, 2);
        CourseDraftPlace s2 = draftPlace(102L, PlaceRole.SELECTED, 3);
        CourseDraftPlace s3 = draftPlace(103L, PlaceRole.SELECTED, 4);
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of(base, s1, s2, s3));

        PlaceOrderUpdateRequest request = new PlaceOrderUpdateRequest(List.of(
                new PlaceOrderItem(100L, 1),
                new PlaceOrderItem(101L, 2),
                new PlaceOrderItem(102L, 4),
                new PlaceOrderItem(103L, 5)
        ));

        assertThatThrownBy(() -> courseDraftService.updatePlaceOrder(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.INVALID_VISIT_ORDER);
    }

    @Test
    void throwsWhenVisitOrderHasDuplicate() {
        CourseDraft draft = courseDraft(10L, member(1L));
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        CourseDraftPlace base = draftPlace(100L, PlaceRole.BASE, 1);
        CourseDraftPlace s1 = draftPlace(101L, PlaceRole.SELECTED, 2);
        CourseDraftPlace s2 = draftPlace(102L, PlaceRole.SELECTED, 3);
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of(base, s1, s2));

        PlaceOrderUpdateRequest request = new PlaceOrderUpdateRequest(List.of(
                new PlaceOrderItem(100L, 1),
                new PlaceOrderItem(101L, 2),
                new PlaceOrderItem(102L, 2)
        ));

        assertThatThrownBy(() -> courseDraftService.updatePlaceOrder(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.INVALID_VISIT_ORDER);
    }

    @Test
    void throwsWhenAnyPlaceIsMissingFromRequest() {
        CourseDraft draft = courseDraft(10L, member(1L));
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        CourseDraftPlace base = draftPlace(100L, PlaceRole.BASE, 1);
        CourseDraftPlace s1 = draftPlace(101L, PlaceRole.SELECTED, 2);
        CourseDraftPlace s2 = draftPlace(102L, PlaceRole.SELECTED, 3);
        CourseDraftPlace s3 = draftPlace(103L, PlaceRole.SELECTED, 4);
        given(courseDraftPlaceRepository.findByCourseDraftOrderByVisitOrderAsc(draft))
                .willReturn(List.of(base, s1, s2, s3));

        PlaceOrderUpdateRequest request = new PlaceOrderUpdateRequest(List.of(
                new PlaceOrderItem(100L, 1),
                new PlaceOrderItem(101L, 2),
                new PlaceOrderItem(102L, 3)
        ));

        assertThatThrownBy(() -> courseDraftService.updatePlaceOrder(10L, 1L, request))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.INVALID_VISIT_ORDER);
    }

    @Test
    void addsSelectedPlaceWithNextVisitOrder() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.PLACE_SELECTING);
        PlaceCategory cafeCategory = placeCategory(1L, true);
        PlaceCategory barCategory = placeCategory(2L, true);
        Place basePlace = availablePlace(21L, cafeCategory);
        Place newPlace = availablePlace(32L, barCategory);
        CourseDraftPlace base = draftPlace(50L, PlaceRole.BASE, 1, basePlace);

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(courseDraftPlaceRepository.findByCourseDraftWithPlaceOrderByVisitOrderAsc(draft))
                .willReturn(List.of(base));
        given(placeRepository.findById(32L)).willReturn(Optional.of(newPlace));
        given(courseDraftPlaceRepository.save(any(CourseDraftPlace.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        PlaceAddResponse response =
                courseDraftService.addPlace(10L, 1L, new PlaceAddRequest(32L));

        assertThat(response.draftStatus()).isEqualTo(CourseDraftStatus.PLACE_SELECTING);
        assertThat(response.addedPlace().placeId()).isEqualTo(32L);
        assertThat(response.addedPlace().visitOrder()).isEqualTo(2);
        assertThat(response.addedPlace().placeRole()).isEqualTo(PlaceRole.SELECTED);
        assertThat(response.selectedPlaceCount()).isEqualTo(1);
        assertThat(response.totalPlaceCount()).isEqualTo(2);
        verify(courseDraftRepository).touchUpdatedAt(10L);
    }

    @Test
    void throwsWhenAddPlaceRequesterIsNotOwner() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.PLACE_SELECTING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        assertThatThrownBy(() -> courseDraftService.addPlace(10L, 2L, new PlaceAddRequest(32L)))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.COURSE_DRAFT_ACCESS_DENIED);
    }

    @Test
    void throwsWhenAddPlaceDraftStatusIsNotPlaceSelecting() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.ORDERING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        assertThatThrownBy(() -> courseDraftService.addPlace(10L, 1L, new PlaceAddRequest(32L)))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.PLACE_ADD_DRAFT_STATUS_CONFLICT);
    }

    @Test
    void throwsWhenBasePlaceIntegrityIsBroken() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.PLACE_SELECTING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(courseDraftPlaceRepository.findByCourseDraftWithPlaceOrderByVisitOrderAsc(draft))
                .willReturn(List.of());

        assertThatThrownBy(() -> courseDraftService.addPlace(10L, 1L, new PlaceAddRequest(32L)))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.INVALID_BASE_PLACE);
    }

    @Test
    void throwsWhenPlaceToAddIsNotFound() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.PLACE_SELECTING);
        Place basePlace = availablePlace(21L, placeCategory(1L, true));
        CourseDraftPlace base = draftPlace(50L, PlaceRole.BASE, 1, basePlace);

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(courseDraftPlaceRepository.findByCourseDraftWithPlaceOrderByVisitOrderAsc(draft))
                .willReturn(List.of(base));
        given(placeRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> courseDraftService.addPlace(10L, 1L, new PlaceAddRequest(99L)))
                .isInstanceOf(PlaceException.class)
                .extracting("errorCode")
                .isEqualTo(PlaceDetailErrorCode.PLACE_NOT_FOUND);
    }

    @Test
    void throwsWhenPlaceToAddIsNotAvailable() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.PLACE_SELECTING);
        Place basePlace = availablePlace(21L, placeCategory(1L, true));
        CourseDraftPlace base = draftPlace(50L, PlaceRole.BASE, 1, basePlace);
        Place inactivePlace = Place.builder()
                .id(32L)
                .area(area(1L, true))
                .placeCategory(placeCategory(2L, true))
                .name("비활성 장소")
                .address("서울 마포구")
                .latitude(37.5)
                .longitude(126.9)
                .exposureStatus(PlaceExposureStatus.ACTIVE)
                .reviewStatus(PlaceReviewStatus.APPROVED)
                .isActive(false)
                .build();

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(courseDraftPlaceRepository.findByCourseDraftWithPlaceOrderByVisitOrderAsc(draft))
                .willReturn(List.of(base));
        given(placeRepository.findById(32L)).willReturn(Optional.of(inactivePlace));

        assertThatThrownBy(() -> courseDraftService.addPlace(10L, 1L, new PlaceAddRequest(32L)))
                .isInstanceOf(PlaceException.class)
                .extracting("errorCode")
                .isEqualTo(ExternalPlaceRegistrationErrorCode.PLACE_NOT_AVAILABLE);
    }

    @Test
    void allowsAnyActivePlaceCategory() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.PLACE_SELECTING);
        Place basePlace = availablePlace(21L, placeCategory(1L, true));
        CourseDraftPlace base = draftPlace(50L, PlaceRole.BASE, 1, basePlace);
        Place otherCategoryPlace = availablePlace(32L, placeCategory(2L, true, "OTHER"));

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(courseDraftPlaceRepository.findByCourseDraftWithPlaceOrderByVisitOrderAsc(draft))
                .willReturn(List.of(base));
        given(placeRepository.findById(32L)).willReturn(Optional.of(otherCategoryPlace));
        given(courseDraftPlaceRepository.save(any(CourseDraftPlace.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        PlaceAddResponse response =
                courseDraftService.addPlace(10L, 1L, new PlaceAddRequest(32L));

        assertThat(response.addedPlace().placeId()).isEqualTo(32L);
    }

    @Test
    void throwsWhenRequestedPlaceIsSameAsBasePlace() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.PLACE_SELECTING);
        Place basePlace = availablePlace(21L, placeCategory(1L, true));
        CourseDraftPlace base = draftPlace(50L, PlaceRole.BASE, 1, basePlace);

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(courseDraftPlaceRepository.findByCourseDraftWithPlaceOrderByVisitOrderAsc(draft))
                .willReturn(List.of(base));
        given(placeRepository.findById(21L)).willReturn(Optional.of(basePlace));

        assertThatThrownBy(() -> courseDraftService.addPlace(10L, 1L, new PlaceAddRequest(21L)))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.BASE_PLACE_RESELECT_CONFLICT);
    }

    @Test
    void throwsWhenPlaceIsAlreadySelected() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.PLACE_SELECTING);
        PlaceCategory cafeCategory = placeCategory(1L, true);
        PlaceCategory barCategory = placeCategory(2L, true);
        Place basePlace = availablePlace(21L, cafeCategory);
        Place selectedPlace = availablePlace(32L, barCategory);
        CourseDraftPlace base = draftPlace(50L, PlaceRole.BASE, 1, basePlace);
        CourseDraftPlace selected = draftPlace(51L, PlaceRole.SELECTED, 2, selectedPlace);

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(courseDraftPlaceRepository.findByCourseDraftWithPlaceOrderByVisitOrderAsc(draft))
                .willReturn(List.of(base, selected));
        given(placeRepository.findById(32L)).willReturn(Optional.of(selectedPlace));

        assertThatThrownBy(() -> courseDraftService.addPlace(10L, 1L, new PlaceAddRequest(32L)))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.SELECTED_PLACE_DUPLICATE);
    }

    @Test
    void allowsMultiplePlacesWithSameCategory() {
        CourseDraft draft = courseDraft(10L, member(1L), CourseDraftStatus.PLACE_SELECTING);
        PlaceCategory cafeCategory = placeCategory(1L, true);
        Place basePlace = availablePlace(21L, cafeCategory);
        Place anotherCafePlace = availablePlace(33L, cafeCategory);
        CourseDraftPlace base = draftPlace(50L, PlaceRole.BASE, 1, basePlace);

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(courseDraftPlaceRepository.findByCourseDraftWithPlaceOrderByVisitOrderAsc(draft))
                .willReturn(List.of(base));
        given(placeRepository.findById(33L)).willReturn(Optional.of(anotherCafePlace));
        given(courseDraftPlaceRepository.save(any(CourseDraftPlace.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        PlaceAddResponse response =
                courseDraftService.addPlace(10L, 1L, new PlaceAddRequest(33L));

        assertThat(response.addedPlace().placeId()).isEqualTo(33L);
    }
}
