package com.example.TODAIT__BE.domain.taxonomy.repository;

import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaceCategoryRepository extends JpaRepository<PlaceCategory, Long> {

    List<PlaceCategory> findAllByIsActiveTrueOrderBySortOrderAsc();

    Optional<PlaceCategory> findByCode(String code);
}
