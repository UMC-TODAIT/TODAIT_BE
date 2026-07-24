package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftFoodCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseDraftFoodCategoryRepository extends JpaRepository<CourseDraftFoodCategory, Long> {

    List<CourseDraftFoodCategory> findByCourseDraft(CourseDraft courseDraft);

    void deleteByCourseDraft(CourseDraft courseDraft);
}
