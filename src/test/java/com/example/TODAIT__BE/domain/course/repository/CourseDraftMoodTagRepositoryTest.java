package com.example.TODAIT__BE.domain.course.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.EntityGraph;

class CourseDraftMoodTagRepositoryTest {

    @Test
    void findByCourseDraftOrderByIdAscFetchesMoodTag() throws NoSuchMethodException {
        Method method = CourseDraftMoodTagRepository.class.getMethod(
                "findByCourseDraftOrderByIdAsc",
                CourseDraft.class
        );

        EntityGraph entityGraph = method.getAnnotation(EntityGraph.class);

        assertThat(entityGraph)
                .as("draft mood tags must fetch moodTag to avoid N+1 while saving a course")
                .isNotNull();
        assertThat(entityGraph.attributePaths()).containsExactly("moodTag");
    }
}
