package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

class CourseDraftCleanupSchedulerTest {

    @Test
    void schedulerUsesConfiguredCronInKoreanTimeZone() throws NoSuchMethodException {
        Method method = CourseDraftCleanupScheduler.class.getMethod("cleanupExpiredTerminalDrafts");

        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        assertThat(scheduled).isNotNull();
        assertThat(scheduled.cron()).isEqualTo("${app.course-draft.cleanup-cron}");
        assertThat(scheduled.zone()).isEqualTo("Asia/Seoul");
    }

    @Test
    void schedulerDelegatesToCleanupService() {
        CourseDraftCleanupService cleanupService = org.mockito.Mockito.mock(CourseDraftCleanupService.class);
        CourseDraftCleanupScheduler scheduler = new CourseDraftCleanupScheduler(cleanupService);

        scheduler.cleanupExpiredTerminalDrafts();

        verify(cleanupService).cleanupExpiredTerminalDrafts();
    }
}
