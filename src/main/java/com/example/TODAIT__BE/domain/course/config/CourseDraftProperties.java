package com.example.TODAIT__BE.domain.course.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.course-draft")
public record CourseDraftProperties(
        @Min(1)
        int terminalRetentionDays,
        @NotBlank
        String cleanupCron,
        @Min(1)
        int cleanupBatchSize,
        @Min(1)
        int cleanupMaxBatches
) {
}
