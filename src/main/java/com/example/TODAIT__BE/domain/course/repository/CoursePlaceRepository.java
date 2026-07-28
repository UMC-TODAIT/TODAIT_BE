package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CoursePlaceRepository extends JpaRepository<CoursePlace, Long> {

    List<CoursePlace> findAllByCourseIdAndPlaceRoleOrderByVisitOrderAsc(
            Long courseId,
            PlaceRole placeRole
    );

    Optional<CoursePlace> findFirstByCourseIdAndIsRepresentativeTrue(
            Long courseId
    );

    @Query("""
            select cp
            from CoursePlace cp
            join fetch cp.course c
            join fetch cp.place p
            where c.id in :courseIds
            order by c.id asc, cp.visitOrder asc
            """)
    List<CoursePlace> findAllWithCourseAndPlaceByCourseIds(
            @Param("courseIds") List<Long> courseIds
    );

    @EntityGraph(attributePaths = "place")
    List<CoursePlace> findAllByCourseIdOrderByVisitOrderAsc(Long courseId);
}
