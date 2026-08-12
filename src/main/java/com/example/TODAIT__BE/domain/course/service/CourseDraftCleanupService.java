package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.config.CourseDraftProperties;
import com.example.TODAIT__BE.domain.course.enums.CourseDraftStatus;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftFoodCategoryRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftMoodTagRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftPlaceRepository;
import com.example.TODAIT__BE.domain.course.repository.CourseDraftRepository;
import com.example.TODAIT__BE.domain.recommendation.repository.RecommendationLogRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseDraftCleanupService {

    private static final List<CourseDraftStatus> TERMINAL_STATUSES = List.of(
            CourseDraftStatus.COMPLETED,
            CourseDraftStatus.ABANDONED
    );

    private final CourseDraftRepository courseDraftRepository;
    private final CourseDraftPlaceRepository courseDraftPlaceRepository;
    private final CourseDraftMoodTagRepository courseDraftMoodTagRepository;
    private final CourseDraftFoodCategoryRepository courseDraftFoodCategoryRepository;
    private final RecommendationLogRepository recommendationLogRepository;
    private final CourseDraftProperties courseDraftProperties;
    private final Clock clock;

    @Transactional
    public CleanupResult cleanupExpiredTerminalDrafts() {
        LocalDateTime now = LocalDateTime.now(clock);
        int batchSize = Math.max(1, courseDraftProperties.cleanupBatchSize());
        List<Long> draftIds = courseDraftRepository.findExpiredTerminalDraftIds(
                TERMINAL_STATUSES,
                now,
                PageRequest.of(0, batchSize)
        );

        if (draftIds.isEmpty()) {
            return new CleanupResult(0, 0);
        }

        recommendationLogRepository.clearCourseDraftReferences(draftIds);
        courseDraftPlaceRepository.deleteByCourseDraftIdIn(draftIds);
        courseDraftMoodTagRepository.deleteByCourseDraftIdIn(draftIds);
        courseDraftFoodCategoryRepository.deleteByCourseDraftIdIn(draftIds);
        int deleted = courseDraftRepository.deleteExpiredTerminalDraftsByIdIn(draftIds, TERMINAL_STATUSES, now);
        return new CleanupResult(draftIds.size(), deleted);
    }

    public record CleanupResult(int fetchedCount, int deletedCount) {

        public boolean fetchedFullBatch(int batchSize) {
            return fetchedCount >= batchSize;
        }
    }
}
