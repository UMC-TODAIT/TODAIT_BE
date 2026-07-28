package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.CourseFoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseFoodCategoryRepository extends JpaRepository<CourseFoodCategory, Long> {
}
