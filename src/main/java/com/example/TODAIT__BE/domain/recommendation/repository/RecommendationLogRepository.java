package com.example.TODAIT__BE.domain.recommendation.repository;

import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationLogRepository
        extends JpaRepository<RecommendationLog, Long> {
}
