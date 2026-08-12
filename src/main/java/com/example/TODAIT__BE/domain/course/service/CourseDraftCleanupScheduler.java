package com.example.TODAIT__BE.domain.course.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseDraftCleanupScheduler {

    private final CourseDraftCleanupService courseDraftCleanupService;

    @Scheduled(cron = "${app.course-draft.cleanup-cron}", zone = "Asia/Seoul")
    public void cleanupExpiredTerminalDrafts() {
        courseDraftCleanupService.cleanupExpiredTerminalDrafts();
    }
}
