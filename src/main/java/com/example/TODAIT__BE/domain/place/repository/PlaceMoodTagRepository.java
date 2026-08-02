package com.example.TODAIT__BE.domain.place.repository;

import com.example.TODAIT__BE.domain.place.entity.PlaceMoodTag;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceMoodTagRepository extends JpaRepository<PlaceMoodTag, Long> {

    @EntityGraph(attributePaths = "moodTag")
    List<PlaceMoodTag> findAllByPlaceIdOrderByIdAsc(Long placeId);
}
