package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.enums.CourseVisibility;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {

    long countByMemberId(Long memberId);

    @Query("""
            select c
            from Course c
            join c.area a
            where c.id = :id
              and c.visibility = :visibility
              and c.sourceType = :sourceType
              and a.isActive = true
            """)
    Optional<Course> findActiveRecommendedCourseById(
            @Param("id") Long id,
            @Param("visibility") CourseVisibility visibility,
            @Param("sourceType") CourseSourceType sourceType
    );

    @Query("""
            select c
            from Course c
            join fetch c.member
            join fetch c.basePlace
            where c.id = :courseId
              and c.deletedAt is null
            """)
    Optional<Course> findSavedCourseDetailById(
            @Param("courseId") Long courseId
    );

    @Query("""
            select c
            from Course c
            join fetch c.area a
            where c.visibility = :visibility
              and c.sourceType = :sourceType
              and c.deletedAt is null
              and a.isActive = true
              and a.code in :areaCodes
            """)
    List<Course> findRecommendedCourseCandidates(
            @Param("visibility") CourseVisibility visibility,
            @Param("sourceType") CourseSourceType sourceType,
            @Param("areaCodes") List<String> areaCodes
    );

    @Query("""
            select c
            from Course c
            join fetch c.basePlace
            where c.member.id = :memberId
              and c.deletedAt is null
            order by c.createdAt desc, c.id desc
            """)
    List<Course> findRecentSavedCourses(
            @Param("memberId") Long memberId,
            Pageable pageable
    );

    @Query("""
            select c
            from Course c
            join fetch c.basePlace
            where c.member.id = :memberId
              and c.deletedAt is null
            order by c.viewCount desc, c.updatedAt desc, c.id desc
            """)
    List<Course> findPopularSavedCourses(
            @Param("memberId") Long memberId,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true)
    @Query("""
            update Course c
            set c.viewCount = coalesce(c.viewCount, 0) + 1
            where c.id = :courseId
            """)
    int increaseViewCount(
            @Param("courseId") Long courseId
    );

    @Query("""
            select c.viewCount
            from Course c
            where c.id = :courseId
            """)
    Integer findViewCountById(
            @Param("courseId") Long courseId
    );
}
