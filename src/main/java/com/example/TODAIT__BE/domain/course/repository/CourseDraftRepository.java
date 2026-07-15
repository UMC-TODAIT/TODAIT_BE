package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseDraftRepository extends JpaRepository<CourseDraft, Long> {
}
