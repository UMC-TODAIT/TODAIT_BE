package com.example.TODAIT__BE.domain.place.repository;

import com.example.TODAIT__BE.domain.place.entity.PlaceImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceImageRepository extends JpaRepository<PlaceImage, Long> {

    List<PlaceImage>
    findAllByPlaceIdInAndIsPrimaryTrueOrderByPlaceIdAscDisplayOrderAsc(
            List<Long> placeIds
    );
}
