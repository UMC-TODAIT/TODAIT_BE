package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursePlaceRepository extends JpaRepository<CoursePlace, Long> {

    List<CoursePlace> findAllByCourseIdOrderByVisitOrderAsc(Long courseId);

    Optional<CoursePlace> findFirstByCourseIdAndIsRepresentativeTrue(
            Long courseId
    );
}
