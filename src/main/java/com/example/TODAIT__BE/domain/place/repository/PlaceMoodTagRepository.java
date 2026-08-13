package com.example.TODAIT__BE.domain.place.repository;

import com.example.TODAIT__BE.domain.place.entity.PlaceMoodTag;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceMoodTagRepository extends JpaRepository<PlaceMoodTag, Long> {

    @EntityGraph(attributePaths = "moodTag")
    List<PlaceMoodTag> findAllByPlaceIdOrderByIdAsc(Long placeId);

    @Query("""
        select pmt.place.id as placeId,
               pmt.moodTag.id as moodTagId
        from PlaceMoodTag pmt
        where pmt.place.id in :placeIds
          and pmt.isConfirmed = true
          and pmt.moodTag.isActive = true
        """)
    List<PlaceMoodTagIdView> findConfirmedMoodTagIdsByPlaceIds(
            @Param("placeIds") List<Long> placeIds
    );

    interface PlaceMoodTagIdView {

        Long getPlaceId();

        Long getMoodTagId();
    }
}
