package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseDraftPlaceRepository extends JpaRepository<CourseDraftPlace, Long> {

    List<CourseDraftPlace> findByCourseDraftOrderByVisitOrderAsc(CourseDraft courseDraft);

    Optional<CourseDraftPlace> findByCourseDraftAndPlaceRole(CourseDraft courseDraft, PlaceRole placeRole);
}
