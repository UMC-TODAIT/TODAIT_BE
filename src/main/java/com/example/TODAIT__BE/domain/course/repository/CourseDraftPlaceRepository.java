package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseDraftPlaceRepository extends JpaRepository<CourseDraftPlace, Long> {

    List<CourseDraftPlace> findByCourseDraftOrderByVisitOrderAsc(CourseDraft courseDraft);

    @Query("""
            select cdp
            from CourseDraftPlace cdp
            join fetch cdp.place p
            join fetch p.placeCategory
            join fetch p.area
            where cdp.courseDraft = :courseDraft
            order by cdp.visitOrder asc
            """)
    List<CourseDraftPlace> findByCourseDraftWithPlaceOrderByVisitOrderAsc(
            @Param("courseDraft") CourseDraft courseDraft
    );

    boolean existsByCourseDraft(CourseDraft courseDraft);

    void deleteByCourseDraft(CourseDraft courseDraft);

    @Query("""
            select cdp
            from CourseDraftPlace cdp
            join fetch cdp.place
            where cdp.courseDraft.id = :courseDraftId
            order by cdp.visitOrder asc
            """)
    List<CourseDraftPlace> findByCourseDraftIdWithPlaceOrderByVisitOrderAsc(
            @Param("courseDraftId") Long courseDraftId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CourseDraftPlace cdp where cdp.courseDraft.id in :courseDraftIds")
    int deleteByCourseDraftIdIn(@Param("courseDraftIds") List<Long> courseDraftIds);
}
