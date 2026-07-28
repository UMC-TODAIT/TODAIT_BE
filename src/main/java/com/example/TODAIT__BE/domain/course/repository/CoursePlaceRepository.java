package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoursePlaceRepository extends JpaRepository<CoursePlace, Long> {

    List<CoursePlace> findAllByCourseIdAndPlaceRoleOrderByVisitOrderAsc(
            Long courseId,
            PlaceRole placeRole
    );

    Optional<CoursePlace> findFirstByCourseIdAndIsRepresentativeTrue(
            Long courseId
    );

    List<CoursePlace> findAllByCourseIdInAndPlaceRoleOrderByCourseIdAscVisitOrderAsc(
            List<Long> courseIds,
            PlaceRole placeRole
    );
}
