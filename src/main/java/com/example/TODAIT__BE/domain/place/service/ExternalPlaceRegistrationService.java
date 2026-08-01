package com.example.TODAIT__BE.domain.place.service;

import com.example.TODAIT__BE.domain.place.entity.DataSource;
import com.example.TODAIT__BE.domain.place.entity.Place;
import com.example.TODAIT__BE.domain.place.entity.PlaceSource;
import com.example.TODAIT__BE.domain.place.repository.PlaceRepository;
import com.example.TODAIT__BE.domain.place.repository.PlaceSourceRepository;
import com.example.TODAIT__BE.domain.taxonomy.entity.Area;
import com.example.TODAIT__BE.domain.taxonomy.entity.PlaceCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExternalPlaceRegistrationService {

    private final PlaceRepository placeRepository;
    private final PlaceSourceRepository placeSourceRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Place register(
            Area area,
            PlaceCategory placeCategory,
            DataSource dataSource,
            String name,
            String address,
            String roadAddress,
            Double latitude,
            Double longitude,
            String phone,
            String subCategory,
            String sourcePlaceId,
            String sourceUrl
    ) {
        Place savedPlace = placeRepository.saveAndFlush(Place.createFromExternalSource(
                area, placeCategory, name, address, roadAddress, latitude, longitude, phone, subCategory
        ));

        placeSourceRepository.saveAndFlush(PlaceSource.builder()
                .place(savedPlace)
                .dataSource(dataSource)
                .sourcePlaceId(sourcePlaceId)
                .sourceUrl(sourceUrl)
                .isPrimary(true)
                .build());

        return savedPlace;
    }
}
