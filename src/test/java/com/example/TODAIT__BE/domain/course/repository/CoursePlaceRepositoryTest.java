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

        assertThat(query.value())
                .contains("order by p.operatorPriority asc")
                .doesNotContain("order by p.operatorPriority desc");
    }
}
