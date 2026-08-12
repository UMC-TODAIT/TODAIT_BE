package com.example.TODAIT__BE.domain.recommendation.repository;

import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecommendationLogRepository
        extends JpaRepository<RecommendationLog, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update RecommendationLog rl
            set rl.courseDraft = null
            where rl.courseDraft.id in :courseDraftIds
            """)
    int clearCourseDraftReferences(@Param("courseDraftIds") List<Long> courseDraftIds);
}
