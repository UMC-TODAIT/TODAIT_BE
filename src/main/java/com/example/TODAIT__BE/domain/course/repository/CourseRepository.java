package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.Course;
import com.example.TODAIT__BE.domain.course.enums.CourseSourceType;
import com.example.TODAIT__BE.domain.course.enums.CourseVisibility;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
