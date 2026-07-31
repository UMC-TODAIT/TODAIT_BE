package com.example.TODAIT__BE.domain.place.repository;

import com.example.TODAIT__BE.domain.place.entity.PlaceFoodCategory;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceFoodCategoryRepository extends JpaRepository<PlaceFoodCategory, Long> {

    @EntityGraph(attributePaths = "foodCategory")
    List<PlaceFoodCategory> findAllByPlaceIdOrderByIdAsc(Long placeId);
}
