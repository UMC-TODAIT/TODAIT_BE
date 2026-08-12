package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.course.dto.request.CourseDraftRequest.MoodTagSaveRequest;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.MoodTagSaveResponse;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftMoodTag;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.code.CourseDraftErrorCode;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftPlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.course.service.validator.CourseDraftValidator;
import com.example.TODAIT__BE.domain.member.entity.Member;
import com.example.TODAIT__BE.domain.taxonomy.code.MoodTagErrorCode;
import com.example.TODAIT__BE.domain.taxonomy.entity.MoodTag;
import com.example.TODAIT__BE.domain.taxonomy.exception.TaxonomyException;
import com.example.TODAIT__BE.domain.taxonomy.repository.MoodTagRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseDraftMoodTagServiceTest {

    @Mock
    private CourseDraftRepository courseDraftRepository;
    @Mock
    private CourseDraftMoodTagRepository courseDraftMoodTagRepository;
    @Mock
    private CourseDraftPlaceRepository courseDraftPlaceRepository;
    @Mock
    private MoodTagRepository moodTagRepository;

    private CourseDraftService courseDraftService;

    @BeforeEach
    void setUp() {
        courseDraftService = new CourseDraftService(
                courseDraftRepository,
                null,
                courseDraftMoodTagRepository,
                null,
                courseDraftPlaceRepository,
                moodTagRepository,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                new CourseDraftValidator(),
                new com.example.TODAIT__BE.domain.course.config.CourseDraftProperties(30, "0 0 3 * * *", 500, 20, true),
                Clock.systemDefaultZone()
        );
    }

    @Test
    void updatesMoodTagsByDiffAndMovesInitialDraftToFoodSelecting() {
        CourseDraft draft = draft(CourseDraftStatus.MOOD_SELECTING);
        MoodTag oldMoodTag = moodTag(1L, "CALM", "차분한");
        MoodTag keptMoodTag = moodTag(2L, "HIP", "힙한");
        MoodTag addedMoodTag = moodTag(3L, "MODERN", "모던한");
        CourseDraftMoodTag oldDraftMoodTag = CourseDraftMoodTag.builder()
                .courseDraft(draft)
                .moodTag(oldMoodTag)
                .build();
        CourseDraftMoodTag keptDraftMoodTag = CourseDraftMoodTag.builder()
                .courseDraft(draft)
                .moodTag(keptMoodTag)
                .build();

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(moodTagRepository.findByIdInAndIsActiveTrue(List.of(2L, 3L)))
                .willReturn(List.of(keptMoodTag, addedMoodTag));
        given(courseDraftMoodTagRepository.findByCourseDraft(draft))
                .willReturn(List.of(oldDraftMoodTag, keptDraftMoodTag));

        MoodTagSaveResponse response = courseDraftService.saveMoodTags(
                10L,
                1L,
                new MoodTagSaveRequest(List.of(2L, 3L))
        );

        verify(courseDraftRepository).findByIdForUpdate(10L);
        verify(courseDraftMoodTagRepository).deleteAll(List.of(oldDraftMoodTag));

        ArgumentCaptor<CourseDraftMoodTag> saveCaptor = ArgumentCaptor.forClass(CourseDraftMoodTag.class);
        verify(courseDraftMoodTagRepository).save(saveCaptor.capture());
        assertThat(saveCaptor.getValue().getMoodTag()).isEqualTo(addedMoodTag);
        assertThat(draft.getUpdatedAt()).isNotNull();
        assertThat(response.draftStatus()).isEqualTo(CourseDraftStatus.FOOD_SELECTING);
        assertThat(response.moodTags()).extracting("moodTagId").containsExactly(2L, 3L);
    }

    @Test
    void deletesDraftPlacesWhenMoodTagsChangedAndPlacesExist() {
        CourseDraft draft = draft(CourseDraftStatus.FOOD_SELECTING);
        MoodTag oldMoodTag = moodTag(1L, "CALM", "차분한");
        MoodTag keptMoodTag = moodTag(2L, "HIP", "힙한");
        MoodTag addedMoodTag = moodTag(3L, "MODERN", "모던한");
        CourseDraftMoodTag oldDraftMoodTag = CourseDraftMoodTag.builder()
                .courseDraft(draft)
                .moodTag(oldMoodTag)
                .build();
        CourseDraftMoodTag keptDraftMoodTag = CourseDraftMoodTag.builder()
                .courseDraft(draft)
                .moodTag(keptMoodTag)
                .build();

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(moodTagRepository.findByIdInAndIsActiveTrue(List.of(2L, 3L)))
                .willReturn(List.of(keptMoodTag, addedMoodTag));
        given(courseDraftMoodTagRepository.findByCourseDraft(draft))
                .willReturn(List.of(oldDraftMoodTag, keptDraftMoodTag));
        given(courseDraftPlaceRepository.existsByCourseDraft(draft)).willReturn(true);

        courseDraftService.saveMoodTags(
                10L,
                1L,
                new MoodTagSaveRequest(List.of(2L, 3L))
        );

        verify(courseDraftPlaceRepository).deleteByCourseDraft(draft);
        assertThat(draft.getUpdatedAt()).isNotNull();
    }

    @Test
    void keepsDraftPlacesWhenMoodTagsChangedButPlacesDoNotExist() {
        CourseDraft draft = draft(CourseDraftStatus.FOOD_SELECTING);
        MoodTag oldMoodTag = moodTag(1L, "CALM", "차분한");
        MoodTag keptMoodTag = moodTag(2L, "HIP", "힙한");
        MoodTag addedMoodTag = moodTag(3L, "MODERN", "모던한");
        CourseDraftMoodTag oldDraftMoodTag = CourseDraftMoodTag.builder()
                .courseDraft(draft)
                .moodTag(oldMoodTag)
                .build();
        CourseDraftMoodTag keptDraftMoodTag = CourseDraftMoodTag.builder()
                .courseDraft(draft)
                .moodTag(keptMoodTag)
                .build();

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(moodTagRepository.findByIdInAndIsActiveTrue(List.of(2L, 3L)))
                .willReturn(List.of(keptMoodTag, addedMoodTag));
        given(courseDraftMoodTagRepository.findByCourseDraft(draft))
                .willReturn(List.of(oldDraftMoodTag, keptDraftMoodTag));
        given(courseDraftPlaceRepository.existsByCourseDraft(draft)).willReturn(false);

        courseDraftService.saveMoodTags(
                10L,
                1L,
                new MoodTagSaveRequest(List.of(2L, 3L))
        );

        verify(courseDraftPlaceRepository, never()).deleteByCourseDraft(draft);
    }

    @Test
    void keepsFoodSelectingStatusWhenMoodTagsAreUpdatedFromFoodScreen() {
        CourseDraft draft = draft(CourseDraftStatus.FOOD_SELECTING);
        MoodTag moodTag = moodTag(1L, "CALM", "차분한");
        MoodTag secondMoodTag = moodTag(2L, "HIP", "힙한");
        CourseDraftMoodTag existingMoodTag = CourseDraftMoodTag.builder()
                .courseDraft(draft)
                .moodTag(moodTag)
                .build();
        CourseDraftMoodTag secondExistingMoodTag = CourseDraftMoodTag.builder()
                .courseDraft(draft)
                .moodTag(secondMoodTag)
                .build();

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(moodTagRepository.findByIdInAndIsActiveTrue(List.of(1L, 2L)))
                .willReturn(List.of(moodTag, secondMoodTag));
        given(courseDraftMoodTagRepository.findByCourseDraft(draft))
                .willReturn(List.of(existingMoodTag, secondExistingMoodTag));

        MoodTagSaveResponse response = courseDraftService.saveMoodTags(
                10L,
                1L,
                new MoodTagSaveRequest(List.of(1L, 2L))
        );

        verify(courseDraftMoodTagRepository).deleteAll(List.of());
        verify(courseDraftMoodTagRepository, never()).save(any());
        verify(courseDraftPlaceRepository, never()).existsByCourseDraft(draft);
        verify(courseDraftPlaceRepository, never()).deleteByCourseDraft(draft);
        assertThat(draft.getUpdatedAt()).isNull();
        assertThat(response.draftStatus()).isEqualTo(CourseDraftStatus.FOOD_SELECTING);
    }

    @Test
    void throwsWhenMoodTagCountIsLessThanMinimum() {
        CourseDraft draft = draft(CourseDraftStatus.MOOD_SELECTING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        assertThatThrownBy(() -> courseDraftService.saveMoodTags(
                10L,
                1L,
                new MoodTagSaveRequest(List.of(1L))
        ))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.INVALID_MOOD_TAG_COUNT);

        verify(moodTagRepository, never()).findByIdInAndIsActiveTrue(any());
        verify(courseDraftMoodTagRepository, never()).findByCourseDraft(any());
    }

    @Test
    void throwsWhenMoodTagCountExceedsMaximum() {
        CourseDraft draft = draft(CourseDraftStatus.MOOD_SELECTING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        assertThatThrownBy(() -> courseDraftService.saveMoodTags(
                10L,
                1L,
                new MoodTagSaveRequest(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L))
        ))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.INVALID_MOOD_TAG_COUNT);

        verify(moodTagRepository, never()).findByIdInAndIsActiveTrue(any());
        verify(courseDraftMoodTagRepository, never()).findByCourseDraft(any());
    }

    @Test
    void throwsWhenMoodTagsAreUpdatedInUnsupportedStatus() {
        CourseDraft draft = draft(CourseDraftStatus.ORDERING);
        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));

        assertThatThrownBy(() -> courseDraftService.saveMoodTags(
                10L,
                1L,
                new MoodTagSaveRequest(List.of(1L, 2L))
        ))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.MOOD_TAG_DRAFT_STATUS_CONFLICT);

        verify(courseDraftMoodTagRepository, never()).findByCourseDraft(any());
    }

    @Test
    void throwsWhenMoodTagIsInactive() {
        CourseDraft draft = draft(CourseDraftStatus.MOOD_SELECTING);

        given(courseDraftRepository.findByIdForUpdate(10L)).willReturn(Optional.of(draft));
        given(moodTagRepository.findByIdInAndIsActiveTrue(List.of(1L, 2L))).willReturn(List.of(mock(MoodTag.class)));

        assertThatThrownBy(() -> courseDraftService.saveMoodTags(
                10L,
                1L,
                new MoodTagSaveRequest(List.of(1L, 2L))
        ))
                .isInstanceOf(TaxonomyException.class)
                .extracting("errorCode")
                .isEqualTo(MoodTagErrorCode.MOOD_TAG_NOT_FOUND);

        verify(courseDraftMoodTagRepository, never()).findByCourseDraft(any());
    }

    private CourseDraft draft(CourseDraftStatus status) {
        return CourseDraft.builder()
                .id(10L)
                .member(Member.builder().id(1L).build())
                .status(status)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
    }

    private MoodTag moodTag(Long id, String code, String name) {
        MoodTag moodTag = mock(MoodTag.class);
        given(moodTag.getId()).willReturn(id);
        lenient().when(moodTag.getCode()).thenReturn(code);
        lenient().when(moodTag.getName()).thenReturn(name);
        return moodTag;
    }
}
