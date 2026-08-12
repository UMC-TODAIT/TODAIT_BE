package com.example.TODAIT__BE.domain.course.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.TODAIT__BE.domain.recommendation.repository.RecommendationLogRepository;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

class CourseDraftCleanupRepositoryTest {

    @Test
    void expiredDraftLookupFiltersTerminalStatusAndExpiryWithDeterministicOrder() throws NoSuchMethodException {
        Method method = CourseDraftRepository.class.getMethod(
                "findExpiredTerminalDraftIds",
                List.class,
                LocalDateTime.class,
                Pageable.class
        );

        Query query = method.getAnnotation(Query.class);

        assertThat(query).isNotNull();
        assertThat(query.value()).contains("cd.status in :statuses");
        assertThat(query.value()).contains("cd.expiresAt <= :now");
        assertThat(query.value()).contains("order by cd.expiresAt asc, cd.id asc");
    }

    @Test
    void draftDeleteRechecksTerminalStatusAndExpiry() throws NoSuchMethodException {
        Method method = CourseDraftRepository.class.getMethod(
                "deleteExpiredTerminalDraftsByIdIn",
                List.class,
                List.class,
                LocalDateTime.class
        );

        Query query = method.getAnnotation(Query.class);

        assertThat(method.getAnnotation(Modifying.class)).isNotNull();
        assertThat(query).isNotNull();
        assertThat(query.value()).contains("cd.id in :ids");
        assertThat(query.value()).contains("cd.status in :statuses");
        assertThat(query.value()).contains("cd.expiresAt <= :now");
    }

    @Test
    void recommendationLogCleanupNullsDraftReference() throws NoSuchMethodException {
        Method method = RecommendationLogRepository.class.getMethod(
                "clearCourseDraftReferences",
                List.class
        );

        Query query = method.getAnnotation(Query.class);

        assertThat(method.getAnnotation(Modifying.class)).isNotNull();
        assertThat(query).isNotNull();
        assertThat(query.value()).contains("set rl.courseDraft = null");
        assertThat(query.value()).contains("rl.courseDraft.id in :courseDraftIds");
    }

    @Test
    void childCleanupRepositoriesUseBulkDeletes() throws NoSuchMethodException {
        assertBulkDelete(CourseDraftPlaceRepository.class);
        assertBulkDelete(CourseDraftMoodTagRepository.class);
        assertBulkDelete(CourseDraftFoodCategoryRepository.class);
    }

    private void assertBulkDelete(Class<?> repositoryType) throws NoSuchMethodException {
        Method method = repositoryType.getMethod("deleteByCourseDraftIdIn", List.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(method.getAnnotation(Modifying.class)).isNotNull();
        assertThat(query).isNotNull();
        assertThat(query.value()).contains("courseDraft.id in :courseDraftIds");
    }
}
