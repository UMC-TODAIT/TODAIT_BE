package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.CourseMoodTag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseMoodTagRepository
        extends JpaRepository<CourseMoodTag, Long> {

    @EntityGraph(attributePaths = "moodTag")
    Optional<CourseMoodTag> findFirstByCourseIdOrderByIdAsc(
            Long courseId
    );

    @EntityGraph(attributePaths = "moodTag")
    List<CourseMoodTag> findAllByCourseIdOrderByIdAsc(
            Long courseId
    );

    @Query("""
            select cmt
            from CourseMoodTag cmt
            join fetch cmt.course c
            join fetch cmt.moodTag mt
            where c.id in :courseIds
            order by c.id asc, cmt.id asc
            """)
    List<CourseMoodTag> findAllWithCourseAndMoodTagByCourseIds(
            @Param("courseIds") List<Long> courseIds
    );
}
