package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.CourseFoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseFoodCategoryRepository extends JpaRepository<CourseFoodCategory, Long> {
    List<CourseFoodCategory> findAllByCourseIdInOrderByCourseIdAscIdAsc(
            List<Long> courseIds
    );
}
