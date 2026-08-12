package com.example.TODAIT__BE.domain.course.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.course-draft")
public record CourseDraftProperties(
        int terminalRetentionDays,
        String cleanupCron,
        int cleanupBatchSize
) {
}
