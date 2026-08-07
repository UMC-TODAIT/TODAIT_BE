package com.example.TODAIT__BE.domain.course.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.enums.CourseVisibility;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceReviewStatus;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

class CoursePlaceRepositoryTest {

    @Test
    void findHotPlaceCandidatesOrdersByOperatorPriorityAscending()
            throws NoSuchMethodException {
        Method method = CoursePlaceRepository.class.getMethod(
                "findHotPlaceCandidates",
                CourseVisibility.class,
                CourseSourceType.class,
                PlaceReviewStatus.class,
                PlaceExposureStatus.class,
                Pageable.class
        );

        Query query = method.getAnnotation(Query.class);

        assertThat(query)
                .as("findHotPlaceCandidates must define an explicit JPQL query")
                .isNotNull();

        String normalizedQuery = query.value()
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase();

        assertThat(normalizedQuery)
                .contains("order by p.operatorpriority asc")
                .doesNotContain("order by p.operatorpriority desc");
    }
}
