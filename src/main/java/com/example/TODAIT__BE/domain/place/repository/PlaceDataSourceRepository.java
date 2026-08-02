package com.example.TODAIT__BE.domain.place.repository;

import com.example.TODAIT__BE.domain.place.entity.PlaceDataSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaceDataSourceRepository extends JpaRepository<PlaceDataSource, Long> {

    Optional<PlaceDataSource> findByCodeAndIsActiveTrue(String code);
}
