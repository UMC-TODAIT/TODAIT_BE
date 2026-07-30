package com.example.TODAIT__BE.domain.recommendation.repository;

import com.example.TODAIT__BE.domain.recommendation.entity.RecommendationResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationResultRepository
        extends JpaRepository<RecommendationResult, Long> {
}
