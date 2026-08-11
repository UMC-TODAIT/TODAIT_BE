package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.course.code.CourseDraftErrorCode;
import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.StatusUpdateRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.StatusUpdateResponse;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftMoodTagRepository;
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

@ExtendWith(MockitoExtension.class)
class CourseDraftStatusUpdateServiceTest {

    private static final Long DRAFT_ID = 10L;
    private static final Long MEMBER_ID = 1L;

    @Mock
    private CourseDraftRepository courseDraftRepository;
    @Mock
    private CourseDraftMoodTagRepository courseDraftMoodTagRepository;
    @Mock
    private CourseDraftFoodCategoryRepository courseDraftFoodCategoryRepository;
    @Mock
    private CourseDraftPlaceRepository courseDraftPlaceRepository;

    private CourseDraftService service;

    @BeforeEach
    void setUp() {
        service = new CourseDraftService(
                courseDraftRepository,
                null,
                courseDraftMoodTagRepository,
                courseDraftFoodCategoryRepository,
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
    void movesBackToMoodSelectingAndClearsAllDraftSelections() {
        CourseDraft draft = draft(CourseDraftStatus.SAVING, MEMBER_ID);
        given(courseDraftRepository.findByIdForUpdate(DRAFT_ID))
                .willReturn(Optional.of(draft));

        StatusUpdateResponse response = service.updateStatus(
                DRAFT_ID,
                MEMBER_ID,
                new StatusUpdateRequest(CourseDraftStatus.MOOD_SELECTING)
        );

        assertThat(response.draftStatus()).isEqualTo(CourseDraftStatus.MOOD_SELECTING);
        assertThat(draft.getStatus()).isEqualTo(CourseDraftStatus.MOOD_SELECTING);
        verify(courseDraftPlaceRepository).deleteByCourseDraft(draft);
        verify(courseDraftFoodCategoryRepository).deleteByCourseDraft(draft);
        verify(courseDraftMoodTagRepository).deleteByCourseDraft(draft);
    }

    @Test
    void movesBackToFoodSelectingAndKeepsMoodTags() {
        CourseDraft draft = draft(CourseDraftStatus.ORDERING, MEMBER_ID);
        given(courseDraftRepository.findByIdForUpdate(DRAFT_ID))
                .willReturn(Optional.of(draft));

        service.updateStatus(
                DRAFT_ID,
                MEMBER_ID,
                new StatusUpdateRequest(CourseDraftStatus.FOOD_SELECTING)
        );

        assertThat(draft.getStatus()).isEqualTo(CourseDraftStatus.FOOD_SELECTING);
        verify(courseDraftPlaceRepository).deleteByCourseDraft(draft);
        verify(courseDraftFoodCategoryRepository).deleteByCourseDraft(draft);
        verify(courseDraftMoodTagRepository, never()).deleteByCourseDraft(draft);
    }

    @Test
    void movesBackToBasePlaceSelectingAndKeepsMoodAndFoodSelections() {
        CourseDraft draft = draft(CourseDraftStatus.PLACE_SELECTING, MEMBER_ID);
        given(courseDraftRepository.findByIdForUpdate(DRAFT_ID))
                .willReturn(Optional.of(draft));

        service.updateStatus(
                DRAFT_ID,
                MEMBER_ID,
                new StatusUpdateRequest(CourseDraftStatus.BASE_PLACE_SELECTING)
        );

        assertThat(draft.getStatus()).isEqualTo(CourseDraftStatus.BASE_PLACE_SELECTING);
        verify(courseDraftPlaceRepository).deleteByCourseDraft(draft);
        verify(courseDraftFoodCategoryRepository, never()).deleteByCourseDraft(draft);
        verify(courseDraftMoodTagRepository, never()).deleteByCourseDraft(draft);
    }

    @Test
    void movesBackToPlaceSelectingAndKeepsPlaces() {
        CourseDraft draft = draft(CourseDraftStatus.SAVING, MEMBER_ID);
        given(courseDraftRepository.findByIdForUpdate(DRAFT_ID))
                .willReturn(Optional.of(draft));

        service.updateStatus(
                DRAFT_ID,
                MEMBER_ID,
                new StatusUpdateRequest(CourseDraftStatus.PLACE_SELECTING)
        );

        assertThat(draft.getStatus()).isEqualTo(CourseDraftStatus.PLACE_SELECTING);
        verify(courseDraftPlaceRepository, never()).deleteByCourseDraft(draft);
        verify(courseDraftFoodCategoryRepository, never()).deleteByCourseDraft(draft);
        verify(courseDraftMoodTagRepository, never()).deleteByCourseDraft(draft);
    }

    @Test
    void movesBackToOrderingAfterValidatingPlaceComposition() {
        CourseDraft draft = draft(CourseDraftStatus.SAVING, MEMBER_ID);
        given(courseDraftRepository.findByIdForUpdate(DRAFT_ID))
                .willReturn(Optional.of(draft));
        given(courseDraftPlaceRepository.findByCourseDraftIdWithPlaceOrderByVisitOrderAsc(DRAFT_ID))
                .willReturn(List.of(
                        draftPlace(100L, 1, PlaceRole.BASE),
                        draftPlace(101L, 2, PlaceRole.SELECTED)
                ));

        service.updateStatus(
                DRAFT_ID,
                MEMBER_ID,
                new StatusUpdateRequest(CourseDraftStatus.ORDERING)
        );

        assertThat(draft.getStatus()).isEqualTo(CourseDraftStatus.ORDERING);
    }

    @Test
    void rejectsForwardOrSameStatusMove() {
        CourseDraft draft = draft(CourseDraftStatus.FOOD_SELECTING, MEMBER_ID);
        given(courseDraftRepository.findByIdForUpdate(DRAFT_ID))
                .willReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.updateStatus(
                DRAFT_ID,
                MEMBER_ID,
                new StatusUpdateRequest(CourseDraftStatus.BASE_PLACE_SELECTING)
        ))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.COURSE_DRAFT_STATUS_CONFLICT);
    }

    @Test
    void rejectsCompletedDraft() {
        CourseDraft draft = draft(CourseDraftStatus.COMPLETED, MEMBER_ID);
        given(courseDraftRepository.findByIdForUpdate(DRAFT_ID))
                .willReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.updateStatus(
                DRAFT_ID,
                MEMBER_ID,
                new StatusUpdateRequest(CourseDraftStatus.PLACE_SELECTING)
        ))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.COURSE_DRAFT_STATUS_CONFLICT);
    }

    @Test
    void rejectsNonOwner() {
        CourseDraft draft = draft(CourseDraftStatus.SAVING, 999L);
        given(courseDraftRepository.findByIdForUpdate(DRAFT_ID))
                .willReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.updateStatus(
                DRAFT_ID,
                MEMBER_ID,
                new StatusUpdateRequest(CourseDraftStatus.PLACE_SELECTING)
        ))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.COURSE_DRAFT_ACCESS_DENIED);
    }

    private CourseDraft draft(CourseDraftStatus status, Long memberId) {
        return CourseDraft.builder()
                .id(DRAFT_ID)
                .member(Member.builder().id(memberId).build())
                .status(status)
                .build();
    }

    private CourseDraftPlace draftPlace(
            Long id,
            int visitOrder,
            PlaceRole placeRole
    ) {
        return CourseDraftPlace.builder()
                .id(id)
                .place(Place.builder().id(id + 1000).build())
                .visitOrder(visitOrder)
                .placeRole(placeRole)
                .build();
    }
}
