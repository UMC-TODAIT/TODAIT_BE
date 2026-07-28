package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursePlaceRepository extends JpaRepository<CoursePlace, Long> {

    @EntityGraph(attributePaths = "place")
    List<CoursePlace> findAllByCourseIdOrderByVisitOrderAsc(Long courseId);

    @EntityGraph(attributePaths = "place")
    Optional<CoursePlace> findFirstByCourseIdAndIsRepresentativeTrue(
            Long courseId
    );
}
