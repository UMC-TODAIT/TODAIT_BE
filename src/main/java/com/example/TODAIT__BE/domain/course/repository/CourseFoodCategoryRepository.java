package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.CourseFoodCategory;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseFoodCategoryRepository
        extends JpaRepository<CourseFoodCategory, Long> {

    @EntityGraph(attributePaths = "foodCategory")
    List<CourseFoodCategory> findAllByCourseIdOrderByIdAsc(
            Long courseId
    );
}
