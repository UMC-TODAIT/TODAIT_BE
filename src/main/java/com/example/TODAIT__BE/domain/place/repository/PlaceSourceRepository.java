package com.example.TODAIT__BE.domain.place.repository;

import com.example.TODAIT__BE.domain.place.entity.DataSource;
import com.example.TODAIT__BE.domain.place.entity.PlaceSource;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceSourceRepository extends JpaRepository<PlaceSource, Long> {

    Optional<PlaceSource> findByDataSourceAndSourcePlaceId(DataSource dataSource, String sourcePlaceId);

    Optional<PlaceSource> findByPlaceIdAndIsPrimaryTrue(Long placeId);
}
