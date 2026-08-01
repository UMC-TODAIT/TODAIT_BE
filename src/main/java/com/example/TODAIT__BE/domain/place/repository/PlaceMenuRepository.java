package com.example.TODAIT__BE.domain.place.repository;

import com.example.TODAIT__BE.domain.place.entity.PlaceMenu;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceMenuRepository extends JpaRepository<PlaceMenu, Long> {

    List<PlaceMenu> findAllByPlaceIdOrderByDisplayOrderAscIdAsc(Long placeId);
}
