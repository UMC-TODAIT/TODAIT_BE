package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.config.CourseDraftProperties;
import com.example.TODAIT__BE.domain.course.service.CourseDraftCleanupService.CleanupResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "app.course-draft",
        name = "cleanup-scheduler-enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class CourseDraftCleanupScheduler {

    private final CourseDraftCleanupService courseDraftCleanupService;
    private final CourseDraftProperties courseDraftProperties;

    @Scheduled(cron = "${app.course-draft.cleanup-cron}", zone = "Asia/Seoul")
    public void cleanupExpiredTerminalDrafts() {
        long startedAt = System.nanoTime();
        int batchSize = courseDraftProperties.cleanupBatchSize();
        int maxBatches = courseDraftProperties.cleanupMaxBatches();
        int totalFetched = 0;
        int totalDeleted = 0;
        int executedBatches = 0;

        try {
            for (int i = 0; i < maxBatches; i++) {
                CleanupResult result = courseDraftCleanupService.cleanupExpiredTerminalDrafts();
                executedBatches++;
                totalFetched += result.fetchedCount();
                totalDeleted += result.deletedCount();

                log.info(
                        "courseDraftCleanup batch completed batchNumber={} fetchedCount={} deletedCount={}",
                        i + 1,
                        result.fetchedCount(),
                        result.deletedCount()
                );

                if (!result.fetchedFullBatch(batchSize)) {
                    return;
                }
            }
        } catch (RuntimeException e) {
            log.error(
                    "courseDraftCleanup failed batchSize={} maxBatches={} executedBatches={}",
                    batchSize,
                    maxBatches,
                    executedBatches,
                    e
            );
            throw e;
        } finally {
            long durationMillis = (System.nanoTime() - startedAt) / 1_000_000;
            log.info(
                    "courseDraftCleanup finished executedBatches={} totalFetchedCount={} totalDeletedCount={} durationMillis={}",
                    executedBatches,
                    totalFetched,
                    totalDeleted,
                    durationMillis
            );
        }
    }
}
