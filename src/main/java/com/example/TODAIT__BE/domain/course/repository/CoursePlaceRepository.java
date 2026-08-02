package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.CoursePlace;
import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.enums.CourseVisibility;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import java.util.List;
import java.util.Optional;

import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceReviewStatus;
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

    @Query("""
        select distinct p
        from CoursePlace cp
        join cp.course c
        join cp.place p
        join fetch p.area a
        join fetch p.placeCategory pc
        where c.visibility = :courseVisibility
          and c.sourceType = :courseSourceType
          and c.deletedAt is null
          and p.isActive = true
          and p.isReviewed = true
          and p.reviewStatus = :reviewStatus
          and p.exposureStatus = :exposureStatus
          and p.deletedAt is null
          and p.latitude is not null
          and p.longitude is not null
          and a.isActive = true
          and pc.isActive = true
        order by p.createdAt asc, p.id asc
        """)
    List<Place> findHotPlaceCandidates(
            @Param("courseVisibility")
            CourseVisibility courseVisibility,

            @Param("courseSourceType")
            CourseSourceType courseSourceType,

            @Param("reviewStatus")
            PlaceReviewStatus reviewStatus,

            @Param("exposureStatus")
            PlaceExposureStatus exposureStatus
    );
}
