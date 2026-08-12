package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.course.config.CourseDraftProperties;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftPlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.course.service.CourseDraftCleanupService.CleanupResult;
import com.example.TODAIT__BE.domain.recommendation.repository.RecommendationLogRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CourseDraftCleanupServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-08-12T03:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Mock
    private CourseDraftRepository courseDraftRepository;
    @Mock
    private CourseDraftPlaceRepository courseDraftPlaceRepository;
    @Mock
    private CourseDraftMoodTagRepository courseDraftMoodTagRepository;
    @Mock
    private CourseDraftFoodCategoryRepository courseDraftFoodCategoryRepository;
    @Mock
    private RecommendationLogRepository recommendationLogRepository;

    private CourseDraftCleanupService service;

    @BeforeEach
    void setUp() {
        service = new CourseDraftCleanupService(
                courseDraftRepository,
                courseDraftPlaceRepository,
                courseDraftMoodTagRepository,
                courseDraftFoodCategoryRepository,
                recommendationLogRepository,
                new CourseDraftProperties(30, "0 0 3 * * *", 500, 20, true),
                FIXED_CLOCK
        );
    }

    @Test
    void returnsZeroWhenExpiredTerminalDraftDoesNotExist() {
        given(courseDraftRepository.findExpiredTerminalDraftIds(
                eq(terminalStatuses()),
                any(LocalDateTime.class),
                any(Pageable.class)
        )).willReturn(List.of());

        CleanupResult result = service.cleanupExpiredTerminalDrafts();

        assertThat(result.fetchedCount()).isZero();
        assertThat(result.deletedCount()).isZero();
        verify(recommendationLogRepository, never()).clearCourseDraftReferences(any());
        verify(courseDraftPlaceRepository, never()).deleteByCourseDraftIdIn(any());
        verify(courseDraftMoodTagRepository, never()).deleteByCourseDraftIdIn(any());
        verify(courseDraftFoodCategoryRepository, never()).deleteByCourseDraftIdIn(any());
        verify(courseDraftRepository, never()).deleteExpiredTerminalDraftsByIdIn(any(), any(), any());
    }

    @Test
    void clearsRecommendationLogsAndDeletesChildRowsBeforeDrafts() {
        List<Long> draftIds = List.of(10L, 11L);
        given(courseDraftRepository.findExpiredTerminalDraftIds(
                eq(terminalStatuses()),
                any(LocalDateTime.class),
                any(Pageable.class)
        )).willReturn(draftIds);
        given(courseDraftRepository.deleteExpiredTerminalDraftsByIdIn(
                eq(draftIds),
                eq(terminalStatuses()),
                any(LocalDateTime.class)
        )).willReturn(2);

        CleanupResult result = service.cleanupExpiredTerminalDrafts();

        assertThat(result.fetchedCount()).isEqualTo(2);
        assertThat(result.deletedCount()).isEqualTo(2);

        InOrder order = inOrder(
                recommendationLogRepository,
                courseDraftPlaceRepository,
                courseDraftMoodTagRepository,
                courseDraftFoodCategoryRepository,
                courseDraftRepository
        );
        order.verify(recommendationLogRepository).clearCourseDraftReferences(draftIds);
        order.verify(courseDraftPlaceRepository).deleteByCourseDraftIdIn(draftIds);
        order.verify(courseDraftMoodTagRepository).deleteByCourseDraftIdIn(draftIds);
        order.verify(courseDraftFoodCategoryRepository).deleteByCourseDraftIdIn(draftIds);
        order.verify(courseDraftRepository)
                .deleteExpiredTerminalDraftsByIdIn(eq(draftIds), eq(terminalStatuses()), any(LocalDateTime.class));
    }

    @Test
    void appliesCleanupBatchSizeToExpiredDraftLookup() {
        CourseDraftCleanupService smallBatchService = new CourseDraftCleanupService(
                courseDraftRepository,
                courseDraftPlaceRepository,
                courseDraftMoodTagRepository,
                courseDraftFoodCategoryRepository,
                recommendationLogRepository,
                new CourseDraftProperties(30, "0 0 3 * * *", 7, 20, true),
                FIXED_CLOCK
        );
        given(courseDraftRepository.findExpiredTerminalDraftIds(
                eq(terminalStatuses()),
                any(LocalDateTime.class),
                any(Pageable.class)
        )).willReturn(List.of());
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        smallBatchService.cleanupExpiredTerminalDrafts();

        verify(courseDraftRepository).findExpiredTerminalDraftIds(
                eq(terminalStatuses()),
                any(LocalDateTime.class),
                pageableCaptor.capture()
        );
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(7);
    }

    private List<CourseDraftStatus> terminalStatuses() {
        return List.of(CourseDraftStatus.COMPLETED, CourseDraftStatus.ABANDONED);
    }
}
