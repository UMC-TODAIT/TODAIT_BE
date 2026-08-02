package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftFoodCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseDraftFoodCategoryRepository extends JpaRepository<CourseDraftFoodCategory, Long> {

    List<CourseDraftFoodCategory> findByCourseDraft(CourseDraft courseDraft);

    void deleteByCourseDraft(CourseDraft courseDraft);

    @Query("""
        select cdfc.foodCategory.id
        from CourseDraftFoodCategory cdfc
        where cdfc.courseDraft.id = :courseDraftId
        """)
    List<Long> findFoodCategoryIdsByCourseDraftId(
            @Param("courseDraftId") Long courseDraftId
    );
}
