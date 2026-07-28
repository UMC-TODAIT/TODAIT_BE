package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.CourseMoodTag;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseMoodTagRepository extends JpaRepository<CourseMoodTag, Long> {

    @EntityGraph(attributePaths = "moodTag")
    Optional<CourseMoodTag> findFirstByCourseIdOrderByIdAsc(
            Long courseId
    );

    List<CourseMoodTag> findAllByCourseIdInOrderByCourseIdAscIdAsc(
            List<Long> courseIds
    );
}
