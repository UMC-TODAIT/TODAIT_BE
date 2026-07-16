package com.example.TODAIT__BE.domain.taxonomy.repository;

import com.example.TODAIT__BE.domain.taxonomy.entity.FoodCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodCategoryRepository extends JpaRepository<FoodCategory, Long> {

    Optional<FoodCategory> findByCode(String code);

    boolean existsByCode(String code);

    List<FoodCategory> findAllByIsActiveTrueOrderBySortOrderAsc();
}
