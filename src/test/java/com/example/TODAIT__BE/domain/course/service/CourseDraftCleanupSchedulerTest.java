package com.example.TODAIT__BE.domain.course.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.TODAIT__BE.domain.course.config.CourseDraftProperties;
import com.example.TODAIT__BE.domain.course.service.CourseDraftCleanupService.CleanupResult;
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
        CourseDraftCleanupScheduler scheduler = new CourseDraftCleanupScheduler(
                cleanupService,
                new CourseDraftProperties(30, "0 0 3 * * *", 500, 20)
        );
        given(cleanupService.cleanupExpiredTerminalDrafts()).willReturn(new CleanupResult(0, 0));

        scheduler.cleanupExpiredTerminalDrafts();

        verify(cleanupService).cleanupExpiredTerminalDrafts();
    }

    @Test
    void schedulerRepeatsUntilLastBatchIsNotFull() {
        CourseDraftCleanupService cleanupService = org.mockito.Mockito.mock(CourseDraftCleanupService.class);
        CourseDraftCleanupScheduler scheduler = new CourseDraftCleanupScheduler(
                cleanupService,
                new CourseDraftProperties(30, "0 0 3 * * *", 500, 20)
        );
        given(cleanupService.cleanupExpiredTerminalDrafts())
                .willReturn(new CleanupResult(500, 100), new CleanupResult(300, 300));

        scheduler.cleanupExpiredTerminalDrafts();

        verify(cleanupService, times(2)).cleanupExpiredTerminalDrafts();
    }

    @Test
    void schedulerStopsAtConfiguredMaxBatches() {
        CourseDraftCleanupService cleanupService = org.mockito.Mockito.mock(CourseDraftCleanupService.class);
        CourseDraftCleanupScheduler scheduler = new CourseDraftCleanupScheduler(
                cleanupService,
                new CourseDraftProperties(30, "0 0 3 * * *", 500, 3)
        );
        given(cleanupService.cleanupExpiredTerminalDrafts()).willReturn(new CleanupResult(500, 100));

        scheduler.cleanupExpiredTerminalDrafts();

        verify(cleanupService, times(3)).cleanupExpiredTerminalDrafts();
    }
}
