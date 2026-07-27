package com.example.TODAIT__BE.domain.place.repository;

import com.example.TODAIT__BE.domain.place.entity.PlaceImage;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceImageRepository extends JpaRepository<PlaceImage, Long> {

    Optional<PlaceImage> findFirstByPlaceIdAndIsPrimaryTrueOrderByDisplayOrderAsc(
            Long placeId
    );
}
