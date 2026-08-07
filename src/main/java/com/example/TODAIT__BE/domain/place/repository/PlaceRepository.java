package com.example.TODAIT__BE.domain.place.repository;

import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.enums.PlaceReviewStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query("""
            select p
            from Place p
            left join fetch p.area
            left join fetch p.placeCategory
            left join fetch p.primaryFoodCategory
            where p.id = :placeId
              and p.deletedAt is null
            """)
    Optional<Place> findDetailById(@Param("placeId") Long placeId);

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
            join p.placeCategory pc
            where lower(p.name) like lower(concat('%', :keyword, '%')) escape '!'
              and p.exposureStatus = :exposureStatus
              and p.isActive = true
              and p.latitude is not null
              and p.longitude is not null
              and pc.isActive = true
              and a.isActive = true
              and p.deletedAt is null
            """)
    List<Place> searchExposablePlacesByName(
            @Param("keyword") String keyword,
            @Param("exposureStatus") PlaceExposureStatus exposureStatus
    );

    @EntityGraph(attributePaths = {
            "area",
            "placeCategory"
    })
    @Query("""
        select distinct p
        from Place p
        join p.area a
        join p.placeCategory pc
        where p.isActive = true
          and p.reviewStatus = :reviewStatus
          and p.exposureStatus = :exposureStatus
          and p.deletedAt is null
          and p.name is not null
          and p.name <> ''
          and p.address is not null
          and p.address <> ''
          and p.latitude is not null
          and p.longitude is not null
          and a.isActive = true
          and pc.isActive = true
          and a.code in :areaCodes
        """)
    List<Place> findHomeRecommendedPlaceCandidates(
            @Param("reviewStatus") PlaceReviewStatus reviewStatus,
            @Param("exposureStatus") PlaceExposureStatus exposureStatus,
            @Param("areaCodes") List<String> areaCodes
    );

    @EntityGraph(attributePaths = {
            "area",
            "placeCategory",
            "primaryFoodCategory"
    })
    @Query("""
        select distinct p
        from Place p
        join p.area a
        join p.placeCategory pc
        where p.isActive = true
          and p.reviewStatus = :reviewStatus
          and p.exposureStatus = :exposureStatus
          and p.deletedAt is null
          and p.name is not null
          and p.name <> ''
          and p.address is not null
          and p.address <> ''
          and p.latitude is not null
          and p.longitude is not null
          and a.isActive = true
          and pc.isActive = true
          and a.code in :areaCodes
          and pc.code = :placeCategoryCode
        """)
    List<Place> findNearBasePlaceRecommendationCandidates(
            @Param("reviewStatus") PlaceReviewStatus reviewStatus,
            @Param("exposureStatus") PlaceExposureStatus exposureStatus,
            @Param("areaCodes") List<String> areaCodes,
            @Param("placeCategoryCode") String placeCategoryCode
    );

}
