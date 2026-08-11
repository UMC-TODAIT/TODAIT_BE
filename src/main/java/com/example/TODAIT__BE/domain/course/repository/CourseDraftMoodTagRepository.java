package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftMoodTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseDraftMoodTagRepository extends JpaRepository<CourseDraftMoodTag, Long> {

    List<CourseDraftMoodTag> findByCourseDraft(CourseDraft courseDraft);

    List<CourseDraftMoodTag> findByCourseDraftOrderByIdAsc(CourseDraft courseDraft);

    void deleteByCourseDraft(CourseDraft courseDraft);

    @Query("""
        select cdmt.moodTag.id
        from CourseDraftMoodTag cdmt
        where cdmt.courseDraft.id = :courseDraftId
        """)
    List<Long> findMoodTagIdsByCourseDraftId(
            @Param("courseDraftId") Long courseDraftId
    );
}
