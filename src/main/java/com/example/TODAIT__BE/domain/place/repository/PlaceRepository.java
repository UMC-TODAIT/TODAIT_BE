package com.example.TODAIT__BE.domain.place.repository;

import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    @EntityGraph(attributePaths = {
            "placeCategory",
            "primaryFoodCategory",
            "placeMoodTags",
            "placeMoodTags.moodTag"
    })
    List<Place> findDistinctByNameContainingAndExposureStatus(
            String keyword,
            PlaceExposureStatus exposureStatus
    );
}
