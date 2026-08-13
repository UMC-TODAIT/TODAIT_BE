package com.example.TODAIT__BE.domain.course.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class CourseDraftPropertiesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsPositiveRetentionAndCleanupSettings() {
        CourseDraftProperties properties = new CourseDraftProperties(30, "0 0 3 * * *", 500, 20, true);

        assertThat(validator.validate(properties)).isEmpty();
        assertThat(properties.cleanupSchedulerEnabled()).isTrue();
    }

    @Test
    void rejectsInvalidRetentionAndCleanupSettings() {
        CourseDraftProperties properties = new CourseDraftProperties(0, "", 0, 0, true);

        assertThat(validator.validate(properties))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains(
                        "terminalRetentionDays",
                        "cleanupCron",
                        "cleanupBatchSize",
                        "cleanupMaxBatches"
                );
    }
}
