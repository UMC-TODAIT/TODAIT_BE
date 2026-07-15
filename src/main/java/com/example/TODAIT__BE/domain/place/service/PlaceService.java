package com.example.TODAIT__BE.domain.place.service;

import com.example.TODAIT__BE.domain.place.dto.response.PlaceResponse;
import com.example.TODAIT__BE.domain.place.dto.response.PlaceSearchResponse;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.repository.PlaceRepository;
import com.example.TODAIT__BE.global.apiPayload.code.GeneralErrorCode;
import com.example.TODAIT__BE.global.apiPayload.exception.ProjectException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;

    public PlaceSearchResponse searchPlaces(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new ProjectException(GeneralErrorCode.BAD_REQUEST);
        }

        List<PlaceResponse> places = placeRepository
                .findByNameContainingAndExposureStatus(keyword, PlaceExposureStatus.VISIBLE)
                .stream()
                .map(PlaceResponse::from)
                .toList();

        return new PlaceSearchResponse(places);
    }
}
