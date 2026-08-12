package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.course.code.CourseDraftErrorCode;
import com.example.TODAIT__BE.domain.course.config.CourseDraftProperties;
import com.example.TODAIT__BE.domain.course.dto.response.CourseDraftResponse.AbandonResponse;
import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.exception.CourseException;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftPlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.course.service.validator.CourseDraftValidator;
import com.example.TODAIT__BE.domain.member.entity.Member;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseDraftAbandonServiceTest {

    private static final Long DRAFT_ID = 10L;
    private static final Long MEMBER_ID = 1L;
    private static final ZoneId TEST_ZONE = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime FIXED_NOW =
            LocalDateTime.of(2026, 8, 12, 12, 0);
    private static final Clock FIXED_CLOCK =
            Clock.fixed(FIXED_NOW.atZone(TEST_ZONE).toInstant(), TEST_ZONE);

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
                new CourseDraftValidator(),
                new CourseDraftProperties(7, "0 0 3 * * *", 500, 20),
                FIXED_CLOCK
        );
    }

    @Test
    void abandonsProgressDraftAndKeepsChildSelections() {
        CourseDraft draft = draft(CourseDraftStatus.ORDERING, MEMBER_ID);
        given(courseDraftRepository.findByIdForUpdate(DRAFT_ID))
                .willReturn(Optional.of(draft));
        AbandonResponse response = service.abandonCourseDraft(DRAFT_ID, MEMBER_ID);
        LocalDateTime expectedExpiresAt = LocalDateTime.now(FIXED_CLOCK).plusDays(7);

        assertThat(draft.getStatus()).isEqualTo(CourseDraftStatus.ABANDONED);
        assertThat(draft.getExpiresAt()).isEqualTo(expectedExpiresAt);
        assertThat(response.courseDraftId()).isEqualTo(DRAFT_ID);
        assertThat(response.draftStatus()).isEqualTo(CourseDraftStatus.ABANDONED);
        assertThat(response.expiresAt()).isEqualTo(expectedExpiresAt);
        verify(courseDraftMoodTagRepository, never()).deleteByCourseDraft(draft);
        verify(courseDraftFoodCategoryRepository, never()).deleteByCourseDraft(draft);
        verify(courseDraftPlaceRepository, never()).deleteByCourseDraft(draft);
    }

    @Test
    void rejectsCompletedDraft() {
        CourseDraft draft = draft(CourseDraftStatus.COMPLETED, MEMBER_ID);
        given(courseDraftRepository.findByIdForUpdate(DRAFT_ID))
                .willReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.abandonCourseDraft(DRAFT_ID, MEMBER_ID))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.COURSE_DRAFT_STATUS_CONFLICT);
    }

    @Test
    void rejectsAlreadyAbandonedDraft() {
        CourseDraft draft = draft(CourseDraftStatus.ABANDONED, MEMBER_ID);
        given(courseDraftRepository.findByIdForUpdate(DRAFT_ID))
                .willReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.abandonCourseDraft(DRAFT_ID, MEMBER_ID))
                .isInstanceOf(CourseException.class)
                .extracting("errorCode")
                .isEqualTo(CourseDraftErrorCode.COURSE_DRAFT_STATUS_CONFLICT);
    }

    @Test
    void rejectsNonOwner() {
        CourseDraft draft = draft(CourseDraftStatus.ORDERING, 999L);
        given(courseDraftRepository.findByIdForUpdate(DRAFT_ID))
                .willReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.abandonCourseDraft(DRAFT_ID, MEMBER_ID))
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
}
