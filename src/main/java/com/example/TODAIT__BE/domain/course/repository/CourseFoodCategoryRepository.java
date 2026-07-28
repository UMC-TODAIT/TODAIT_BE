package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.CourseFoodCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseFoodCategoryRepository extends JpaRepository<CourseFoodCategory, Long> {
    @Query("""
            select cfc
            from CourseFoodCategory cfc
            join fetch cfc.course c
            join fetch cfc.foodCategory fc
            where c.id in :courseIds
            order by c.id asc, cfc.id asc
            """)
    List<CourseFoodCategory> findAllWithCourseAndFoodCategoryByCourseIds(
            @Param("courseIds") List<Long> courseIds
    );
}
