package com.example.TODAIT__BE.domain.place.service;

import com.example.TODAIT__BE.domain.place.dto.response.PlaceResponse;
import com.example.TODAIT__BE.domain.place.dto.response.PlaceSearchResponse;
import com.example.TODAIT__BE.domain.place.enums.PlaceExposureStatus;
import com.example.TODAIT__BE.domain.place.exception.PlaceException;
import com.example.TODAIT__BE.domain.place.repository.PlaceRepository;
import com.example.TODAIT__BE.domain.place.code.PlaceErrorCode;
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
            throw new PlaceException(PlaceErrorCode.INVALID_SEARCH_KEYWORD);
        }

        List<PlaceResponse> places = placeRepository
                .searchExposablePlacesByName(
                        escapeLikeKeyword(keyword.trim()),
                        PlaceExposureStatus.ACTIVE
                )
                .stream()
                .map(PlaceResponse::from)
                .toList();

        return new PlaceSearchResponse(places);
    }

    private String escapeLikeKeyword(String keyword) {
        return keyword
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }
}
