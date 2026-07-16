package com.example.TODAIT__BE.domain.place.repository;

import com.example.TODAIT__BE.domain.place.entity.PlaceFoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceFoodCategoryRepository extends JpaRepository<PlaceFoodCategory, Long> {
}
