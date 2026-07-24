package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftMoodTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseDraftMoodTagRepository extends JpaRepository<CourseDraftMoodTag, Long> {

    List<CourseDraftMoodTag> findByCourseDraft(CourseDraft courseDraft);

    void deleteByCourseDraft(CourseDraft courseDraft);
}
