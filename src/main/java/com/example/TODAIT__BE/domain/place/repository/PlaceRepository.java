package com.example.TODAIT__BE.domain.place.repository;

import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    @EntityGraph(attributePaths = {
            "placeCategory",
            "primaryFoodCategory",
            "placeMoodTags",
            "placeMoodTags.moodTag"
    })
    @Query("""
            select distinct p
            from Place p
            join p.area a
            where lower(p.name) like lower(concat('%', :keyword, '%'))
              and p.exposureStatus = :exposureStatus
              and p.isActive = true
              and p.latitude is not null
              and p.longitude is not null
              and p.placeCategory is not null
              and a.isActive = true
              and p.deletedAt is null
            """)
    List<Place> searchExposablePlacesByName(
            @Param("keyword") String keyword,
            @Param("exposureStatus") PlaceExposureStatus exposureStatus
    );
}
