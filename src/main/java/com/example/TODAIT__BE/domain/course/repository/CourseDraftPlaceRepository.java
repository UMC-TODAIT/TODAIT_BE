package com.example.TODAIT__BE.domain.course.repository;

import com.example.TODAIT__BE.domain.course.entity.CourseDraft;
import com.example.TODAIT__BE.domain.course.entity.CourseDraftPlace;
import com.example.TODAIT__BE.domain.course.enums.PlaceRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseDraftPlaceRepository extends JpaRepository<CourseDraftPlace, Long> {

    List<CourseDraftPlace> findByCourseDraftOrderByVisitOrderAsc(CourseDraft courseDraft);

    List<CourseDraftPlace> findByCourseDraftAndPlaceRole(CourseDraft courseDraft, PlaceRole placeRole);
}
