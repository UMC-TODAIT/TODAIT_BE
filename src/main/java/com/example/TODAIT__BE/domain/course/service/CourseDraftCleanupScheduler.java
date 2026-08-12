package com.example.TODAIT__BE.domain.course.service;

import com.example.TODAIT__BE.domain.course.config.CourseDraftProperties;
import com.example.TODAIT__BE.domain.course.service.CourseDraftCleanupService.CleanupResult;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseDraftCleanupScheduler {

    private final CourseDraftCleanupService courseDraftCleanupService;
    private final CourseDraftProperties courseDraftProperties;

    @Scheduled(cron = "${app.course-draft.cleanup-cron}", zone = "Asia/Seoul")
    public void cleanupExpiredTerminalDrafts() {
        int batchSize = courseDraftProperties.cleanupBatchSize();
        for (int i = 0; i < courseDraftProperties.cleanupMaxBatches(); i++) {
            CleanupResult result = courseDraftCleanupService.cleanupExpiredTerminalDrafts();
            if (!result.fetchedFullBatch(batchSize)) {
                return;
            }
        }
    }
}
